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
        //todo: separar as requisições. Uma para criar o perfil do cliente e outra pra fazer a recomendação.
        //todo: isso possibilita a diminuição do prompt
        if (!(entidade instanceof RequisicaoIA requisicao)) {
            return Resultado.erro("Entidade inválida para recomendação com IA.");
        }

        if ((requisicao.getCliente() == null || requisicao.getCliente().getCpf() == null) || requisicao.getPergunta() == null) {
            return Resultado.erro("CPF e pergunta devem ser informados.");
        }

        PedidoDAO pedidoDAO = new PedidoDAO();
        Pedido pedido = new Pedido();
        ClienteEndereco clienteEndereco = new ClienteEndereco();
        clienteEndereco.setCliente(requisicao.getCliente());
        pedido.setClienteEndereco(clienteEndereco);
        Resultado<List<EntidadeDominio>> resultadoPedidos = pedidoDAO.consultar(pedido);
        List<EntidadeDominio> pedidos = resultadoPedidos.getValor();

        PedidoProdutoDAO pedidoProdutoDAO = new PedidoProdutoDAO();
        List<EntidadeDominio> pedidoProdutos = new ArrayList<>();
        for(EntidadeDominio entidadeDominio : pedidos){
            Pedido ped = (Pedido) entidadeDominio;
            PedidoProduto pedProduto = new PedidoProduto();
            pedProduto.setPedido(ped);
            Resultado<List<EntidadeDominio>> resultadoPedidoProdutos = pedidoProdutoDAO.consultar(pedProduto);
            pedidoProdutos.addAll(resultadoPedidoProdutos.getValor());
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
            historico.append("Marca do produto: ").append(pro.getMarca().getNome()).append("\n");
        }

        StringBuilder prompt = new StringBuilder();
        if (historico.isEmpty()){
            prompt.append("O cliente perguntou: ").append(requisicao.getPergunta());
        }else{
            prompt.append("O cliente comprou anteriormente: ")
                    .append(historico)
                    .append(". Com base nisso, responda: ")
                    .append(requisicao.getPergunta());

        }

        ProdutoDAO produtoDAO = new ProdutoDAO();
        Produto produto = new Produto();
        Resultado<List<EntidadeDominio>> resultadoProdutos = produtoDAO.consultar(produto);
        List<EntidadeDominio> produtos = resultadoProdutos.getValor();

        StringBuilder produtosBanco = new StringBuilder();
        for(EntidadeDominio entidadeDominio : produtos){
            Produto pro = (Produto) entidadeDominio;
            produtosBanco.append("Nome produto: ").append(pro.getNome()).append("\n");
            produtosBanco.append("Modelo produto: ").append(pro.getModelo()).append("\n");
            produtosBanco.append("Tamanho do produto: ").append(pro.getTamanho()).append("\n");
            produtosBanco.append("Categoria do produto: ").append(pro.getCategoria().getNome()).append("\n");
            produtosBanco.append("Gênero do produto: ").append(pro.getGenero()).append("\n");
            produtosBanco.append("Marca do produto: ").append(pro.getMarca().getNome()).append("\n");
        }

        prompt.append("Os produtos disponíveis para você recomendar são: ").append(produtosBanco).append(".");

        Resultado<String> resultadoIA = GeminiService.consultarIA(prompt.toString());

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
