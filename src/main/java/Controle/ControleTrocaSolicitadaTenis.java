package Controle;

import Dominio.*;
import Enums.Status;
import Fachada.Fachada;
import Util.Resultado;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.util.List;

@WebServlet(name = "ControleTrocaSolicitadaProduto", urlPatterns = "/controleTrocaProduto")
public class ControleTrocaSolicitadaTenis extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        Resultado<TrocaSolicitadaTenis> trocaTenisFiltro = extrairTrocaTenisFiltro(req);

        Fachada fachada = new Fachada();
        TrocaSolicitadaTenis trocaTenis = trocaTenisFiltro.getValor();
        Resultado<List<EntidadeDominio>> resultadoConsultaTroca = fachada.consultar(trocaTenis);

        if (!resultadoConsultaTroca.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", resultadoConsultaTroca.getErro());
            out.print(gson.toJson(resposta));
            return;
        }

        String json = gson.toJson(resultadoConsultaTroca.getValor());
        resp.setStatus(HttpServletResponse.SC_OK);
        out.print(json);
    }

    private Resultado<TrocaSolicitadaTenis> extrairTrocaTenisFiltro(HttpServletRequest req) {
        TrocaSolicitadaTenis trocaTenisFiltro = new TrocaSolicitadaTenis();

        if(req.getParameter("id") != null){
            trocaTenisFiltro.setId(Integer.parseInt(req.getParameter("id")));
        }
        if(req.getParameter("qtd") != null){
            trocaTenisFiltro.setQuantidade(Integer.parseInt(req.getParameter("qtd")));
        }

        TrocaSolicitada trocaFiltro = new TrocaSolicitada();
        if(req.getParameter("idTroca") != null){
            trocaFiltro.setId(Integer.parseInt(req.getParameter("idTroca")));
        }
        if(req.getParameter("status") != null){
            trocaFiltro.setStatus(Status.valueOf(req.getParameter("status")));
        }

        Pedido pedidoFiltro = new Pedido();
        if(req.getParameter("idPedido") != null){
            pedidoFiltro.setId(Integer.parseInt(req.getParameter("idPedido")));
        }
        if(req.getParameter("statusPedido") != null){
            pedidoFiltro.setId(Integer.parseInt(req.getParameter("statusPedido")));
        }

        Cliente cli = new Cliente();
        if(req.getParameter("idCliente") != null){
            cli.setId(Integer.parseInt(req.getParameter("idCliente")));
        }

        trocaFiltro.setPedido(pedidoFiltro);
        trocaFiltro.setCliente(cli);
        trocaTenisFiltro.setTroca(trocaFiltro);

        return Resultado.sucesso(trocaTenisFiltro);
    }
}
