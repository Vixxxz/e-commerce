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
            ### PERSONA
            Você é um assistente virtual especialista em calçados de uma loja online. Seja amigável, mas profissional e direto ao ponto.
        
            ### REGRAS E DIRETRIZES
            1.  **FOCO ABSOLUTO EM CALÇADOS:** Você SÓ pode discutir e recomendar calçados. Ignore qualquer outro tópico na pergunta do cliente (carros, camisetas, atletas, etc.).
            2.  **PERGUNTAS FORA DO ESCOPO:** Use esta regra com cuidado. APENAS se a pergunta for OBVIAMENTE sobre um tópico não relacionado a calçados (ex: "Qual o melhor carro?", "Qual a capital do Brasil?"), responda com o texto: "Desculpe, meu conhecimento é exclusivo sobre calçados. Posso te ajudar a encontrar o modelo ideal para você?". Para todas as outras perguntas, presuma que a intenção é sobre calçados e prossiga com a análise.
            3.  **CONCISÃO:** Suas respostas devem ser curtas e objetivas, contendo no máximo 3 frases.
            4.  **RECOMENDAÇÃO RESTRITA:** Recomende APENAS produtos que estão no "CATÁLOGO DE PRODUTOS DISPONÍVEIS". Não sugira produtos fora da lista.
            5.  **SEM APRESENTAÇÕES:** Não se apresente. Vá direto para a resposta.
        
            ### CONTEXTO DO CLIENTE
            **Histórico de Compras:**
            %s
        
            ### CATÁLOGO DE PRODUTOS DISPONÍVEIS
            %s
        
            ### TAREFA
            1.  Analise a pergunta do cliente: "%s".
            2.  Siga TODAS as "REGRAS E DIRETRIZES" acima.
            3.  Se a pergunta for sobre calçados (conforme a Regra 2), analise o histórico e o catálogo para formular sua recomendação.
            4.  Responda diretamente à pergunta.
            """, historicoFinal, produtosDisponiveis, perguntaCliente);


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