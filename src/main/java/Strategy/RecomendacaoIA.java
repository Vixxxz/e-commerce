package Strategy;

import Dao.PedidoDAO;
import Dao.PedidoProdutoDAO;
import Dao.ProdutoDAO;
import Dominio.*;
import Service.GeminiService;
import Util.Resultado;

import java.util.ArrayList;
import java.util.List;

public class RecomendacaoIA implements IStrategy{

    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        // Seus TODOs originais ainda são válidos para futuras melhorias.
        //todo: separar as requisições. Uma para criar o perfil do cliente e outra pra fazer a recomendação.
        //todo: isso possibilita a diminuição do prompt
        if (!(entidade instanceof RequisicaoIA requisicao)) {
            return Resultado.erro("Entidade inválida para recomendação com IA.");
        }

        if ((requisicao.getCliente() == null || requisicao.getCliente().getCpf() == null) || requisicao.getPergunta() == null) {
            return Resultado.erro("CPF e pergunta devem ser informados.");
        }

        // A LÓGICA DE BUSCA DE DADOS FOI MANTIDA EXATAMENTE COMO ESTAVA
        PedidoDAO pedidoDAO = new PedidoDAO();
        Pedido pedido = new Pedido();
        ClienteEndereco clienteEndereco = new ClienteEndereco();
        clienteEndereco.setCliente(requisicao.getCliente());
        pedido.setClienteEndereco(clienteEndereco);
        Resultado<List<EntidadeDominio>> resultadoPedidos = pedidoDAO.consultar(pedido);
        List<EntidadeDominio> pedidos = resultadoPedidos.getValor();

        PedidoProdutoDAO pedidoProdutoDAO = new PedidoProdutoDAO();
        List<EntidadeDominio> pedidoProdutos = new ArrayList<>();
        if (pedidos != null) {
            for(EntidadeDominio entidadeDominio : pedidos){
                Pedido ped = (Pedido) entidadeDominio;
                PedidoProduto pedProduto = new PedidoProduto();
                pedProduto.setPedido(ped);
                Resultado<List<EntidadeDominio>> resultadoPedidoProdutos = pedidoProdutoDAO.consultar(pedProduto);
                if (resultadoPedidoProdutos.isSucesso()) {
                    pedidoProdutos.addAll(resultadoPedidoProdutos.getValor());
                }
            }
        }

        StringBuilder historico = new StringBuilder();
        for(EntidadeDominio entidadeDominio : pedidoProdutos){
            PedidoProduto pedPro = (PedidoProduto) entidadeDominio;
            Produto pro = pedPro.getProduto();
            historico.append("Quantidade: ").append(pedPro.getQuantidade()).append("\n");
            historico.append("Nome produto: ").append(pro.getNome()).append("\n");
            historico.append("Modelo produto: ").append(pro.getModelo()).append("\n");
            historico.append("Tamanho do produto: ").append(pro.getTamanho()).append("\n");
            historico.append("Categoria do produto: ").append(pro.getCategoria().getNome()).append("\n");
            historico.append("Gênero do produto: ").append(pro.getGenero()).append("\n");
            historico.append("Marca do produto: ").append(pro.getMarca().getNome()).append("\n\n");
        }

        ProdutoDAO produtoDAO = new ProdutoDAO();
        Produto produto = new Produto();
        Resultado<List<EntidadeDominio>> resultadoProdutos = produtoDAO.consultar(produto);
        List<EntidadeDominio> produtos = resultadoProdutos.getValor();

        StringBuilder produtosBanco = new StringBuilder();
        if (produtos != null) {
            for(EntidadeDominio entidadeDominio : produtos){
                Produto pro = (Produto) entidadeDominio;
                produtosBanco.append("Nome produto: ").append(pro.getNome()).append("\n");
                produtosBanco.append("Modelo produto: ").append(pro.getModelo()).append("\n");
                produtosBanco.append("Tamanho do produto: ").append(pro.getTamanho()).append("\n");
                produtosBanco.append("Categoria do produto: ").append(pro.getCategoria().getNome()).append("\n");
                produtosBanco.append("Gênero do produto: ").append(pro.getGenero()).append("\n");
                produtosBanco.append("Marca do produto: ").append(pro.getMarca().getNome()).append("\n\n");
            }
        }

        // 1. Preparamos as partes do prompt
        String historicoFinal = historico.isEmpty()
                ? "O cliente é novo e não possui histórico de compras."
                : historico.toString();

        String produtosDisponiveis = produtosBanco.isEmpty()
                ? "Nenhum produto encontrado no catálogo."
                : produtosBanco.toString();

        String perguntaCliente = requisicao.getPergunta();

        // 2. Montamos o prompt final usando o novo template estruturado
        String promptFinal = String.format("""
                ### PERSONA E OBJETIVO
                Você é um assistente virtual especialista da loja de calçados. Seu objetivo é ajudar os clientes a encontrar os melhores produtos, fornecendo recomendações úteis e personalizadas. Seja sempre amigável, prestativo e profissional.

                ### CONTEXTO DO CLIENTE
                Abaixo estão os dados do cliente para sua análise.

                **Histórico de Compras Anteriores:**
                %s

                **Pergunta Atual do Cliente:**
                "%s"

                ### CATÁLOGO DE PRODUTOS DISPONÍVEIS
                Estes são os produtos que você pode usar para a sua recomendação. Recomende APENAS produtos desta lista.
                %s

                ### SUA TAREFA
                Com base em TODAS as informações acima, responda diretamente à pergunta do cliente.
                1. Analise o histórico de compras para entender as preferências do cliente (marcas, estilos, categorias, etc.).
                2. Use esse entendimento para selecionar os produtos mais adequados da lista de produtos disponíveis.
                3. Responda de forma conversacional e humana, como se estivesse atendendo o cliente em uma loja real.
                4. Se o histórico de compras for "O cliente é novo e não possui histórico de compras.", baseie sua recomendação inteiramente na pergunta do cliente e nos produtos disponíveis.
                5. Seja direto e não se apresente novamente em todas as respostas. Apenas forneça a recomendação.
                6. Fale apenas sobre calçados, não entre em outros assuntos.
                """, historicoFinal, perguntaCliente, produtosDisponiveis);


        // 3. Fazemos uma ÚNICA chamada para a IA com o prompt completo e otimizado
        Resultado<String> resultadoIA = GeminiService.consultarIA(promptFinal);

        // A lógica de tratamento do resultado final foi mantida
        if (resultadoIA.isSucesso()) {
            requisicao.setResposta(resultadoIA.getValor());
            return Resultado.sucesso(resultadoIA.getValor());
        } else {
            String erro = "Erro na resposta da IA: " + resultadoIA.getErro();
            sb.append(erro);
            requisicao.setResposta(erro);
            return Resultado.erro(erro);
        }
    }
}