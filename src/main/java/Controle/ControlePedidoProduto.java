package Controle;

import Dominio.*;
import Enums.Status;
import Fachada.Fachada;
import Fachada.IFachada;
import Util.Resultado;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "ControlePedidoProduto", urlPatterns = "/controlePedidoProduto")
public class ControlePedidoProduto extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        Resultado<PedidoProduto> resultadoPedidoProdutoFiltro = extrairPedidoFiltro(req);

        if (!resultadoPedidoProdutoFiltro.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "{" + resultadoPedidoProdutoFiltro.getErro() + "}");
            out.print(gson.toJson(resposta));
            return;
        }

        IFachada fachada = new Fachada();
        PedidoProduto pedidoFiltro = resultadoPedidoProdutoFiltro.getValor();
        Resultado<List<EntidadeDominio>> resultadoConsultaPedidoProduto = fachada.consultar(pedidoFiltro);

        if (!resultadoConsultaPedidoProduto.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "{\"erro\": \"" + resultadoConsultaPedidoProduto.getErro() + "\"}");
            out.print(gson.toJson(resposta));
            return;
        }

        String json = gson.toJson(resultadoConsultaPedidoProduto.getValor());
        resp.setStatus(HttpServletResponse.SC_OK);
        out.print(json);

    }

    private Resultado<PedidoProduto> extrairPedidoFiltro(HttpServletRequest req) {
        PedidoProduto pedidoProduto = new PedidoProduto();
        Pedido pedido = new Pedido();
        Produto produto = new Produto();

        if(req.getParameter("idPedido") != null){
            pedido.setId(Integer.parseInt(req.getParameter("idPedido")));
        }

        if(req.getParameter("idProduto") != null){
            produto.setId(Integer.valueOf(req.getParameter("idProduto")));
        }

        if(req.getParameter("id") != null){
            pedidoProduto.setId(Integer.valueOf(req.getParameter("id")));
        }

        pedidoProduto.setPedido(pedido);
        pedidoProduto.setProduto(produto);

        return Resultado.sucesso(pedidoProduto);
    }
}