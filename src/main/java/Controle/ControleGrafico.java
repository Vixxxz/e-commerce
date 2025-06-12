package Controle;

import Dominio.*;
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
import java.util.List;

@WebServlet(name = "ControleGrafico", urlPatterns = "/dashboard")
public class ControleGrafico extends HttpServlet{
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        Resultado<Grafico> resultadoGraficoFiltro = extrairGraficoFiltro(req);

        if (!resultadoGraficoFiltro.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "{" + resultadoGraficoFiltro.getErro() + "}");
            out.print(gson.toJson(resposta));
            return;
        }

        IFachada fachada = new Fachada();
        Grafico graficoFiltro = resultadoGraficoFiltro.getValor();
        Resultado<List<EntidadeDominio>> resultadoConsultaPedido = fachada.consultar(graficoFiltro);

        if (!resultadoConsultaPedido.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "{\"erro\": \"" + resultadoConsultaPedido.getErro() + "\"}");
            out.print(gson.toJson(resposta));
            return;
        }

        String json = gson.toJson(resultadoConsultaPedido.getValor());
        resp.setStatus(HttpServletResponse.SC_OK);
        out.print(json);

    }

    private Resultado<Grafico> extrairGraficoFiltro(HttpServletRequest req) {
        Grafico grafico = new Grafico();
        if(req.getParameter("dataInicio") != null){
            java.sql.Date dataSql = java.sql.Date.valueOf(req.getParameter("dataInicio"));
            java.util.Date dataUtil = new java.util.Date(dataSql.getTime());
            grafico.setDataInicio(dataUtil);
        }
        if(req.getParameter("dataFim") != null){
            java.sql.Date dataSql = java.sql.Date.valueOf(req.getParameter("dataInicio"));
            java.util.Date dataUtil = new java.util.Date(dataSql.getTime());
            grafico.setDataFim(dataUtil);
        }

        return Resultado.sucesso(grafico);
    }
}
