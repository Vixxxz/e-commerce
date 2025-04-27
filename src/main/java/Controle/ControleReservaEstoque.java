package Controle;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import Dominio.*;
import Enums.Ativo;
import Fachada.*;
import Util.Resultado;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

@WebServlet(name = "ControleReservaEstoque", urlPatterns = "/reservaEstoque")
public class ControleReservaEstoque extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();
        String sessaoId = session.getId();

        Gson gson = new Gson();

        Resultado<JsonObject> ResultJsonObject = lerJsonComoObjeto(req);

        if (!ResultJsonObject.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", ResultJsonObject.getErro());
            out.print(gson.toJson(resposta));
            return;
        }

        JsonObject jsonObject = ResultJsonObject.getValor();
        if (!jsonObject.has("reserva")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes");
            out.print(gson.toJson(resposta));
            return;
        }

        Type reservaListType = new TypeToken<List<ReservaEstoque>>() {
        }.getType();
        List<ReservaEstoque> reservas = gson.fromJson(jsonObject.get("reserva"), reservaListType);

        for(ReservaEstoque re : reservas) {
            if(re.getProduto().getId() == null || re.getMarca().getId() == null || re.getQuantidade() <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject resposta = new JsonObject();
                resposta.addProperty("erro", "Dados de reserva inválidos");
                out.print(gson.toJson(resposta));
                return;
            }
            re.setSessao(sessaoId);
        }

        Fachada fachada = new Fachada();

        for(ReservaEstoque re : reservas){
            Resultado<String> resultado = fachada.salvar(re);
            if (!resultado.isSucesso()) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject resposta = new JsonObject();
                resposta.addProperty("erro", resultado.getErro());
                out.print(gson.toJson(resposta));
                return;
            }
        }

        String json = gson.toJson("Reserva feita com sucesso!");
        resp.setStatus(HttpServletResponse.SC_CREATED);
        out.print(json);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession(false);
        String sessaoId = session.getId();
        Gson gson = new Gson();

        Resultado<ReservaEstoque> resultadoReservaFiltro = extrairReservaFiltro(req);

        IFachada fachada = new Fachada();
        ReservaEstoque reservaFiltro = resultadoReservaFiltro.getValor();
        reservaFiltro.setSessao(sessaoId);
        Resultado<List<EntidadeDominio>> resultadoConsultaReserva = fachada.consultar(reservaFiltro);

        if (!resultadoConsultaReserva.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", resultadoConsultaReserva.getErro());
            out.print(gson.toJson(resposta));
            return;
        }

        String json = gson.toJson(resultadoConsultaReserva.getValor());
        resp.setStatus(HttpServletResponse.SC_OK);
        out.print(json);

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();
        String sessaoId = session.getId();

        Gson gson = new Gson();

        Resultado<JsonObject> ResultJsonObject = lerJsonComoObjeto(req);

        if (!ResultJsonObject.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", ResultJsonObject.getErro());
            out.print(gson.toJson(resposta));
            return;
        }

        JsonObject jsonObject = ResultJsonObject.getValor();
        if (!jsonObject.has("reserva")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes");
            out.print(gson.toJson(resposta));
            return;
        }

        Type reservaListType = new TypeToken<List<ReservaEstoque>>() {
        }.getType();
        List<ReservaEstoque> reservas = gson.fromJson(jsonObject.get("reserva"), reservaListType);

        for(ReservaEstoque re : reservas) {
            if(re.getProduto().getId() == null || re.getMarca().getId() == null || re.getQuantidade() <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject resposta = new JsonObject();
                resposta.addProperty("erro", "Dados de reserva inválidos");
                out.print(gson.toJson(resposta));
                return;
            }
            re.setSessao(sessaoId);
        }

        Fachada fachada = new Fachada();

        for(ReservaEstoque re : reservas){
            Resultado<String> resultado = fachada.alterar(re);
            if (!resultado.isSucesso()) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject resposta = new JsonObject();
                resposta.addProperty("erro", resultado.getErro());
                out.print(gson.toJson(resposta));
                return;
            }
        }

        String json = gson.toJson("Reserva atualizada com sucesso!");
        resp.setStatus(HttpServletResponse.SC_CREATED);
        out.print(json);
    }

    private Resultado<JsonObject> lerJsonComoObjeto(HttpServletRequest req) throws IOException {
        String json = lerJsonComoString(req);
        if (json.isBlank()) {
            return Resultado.erro("JSON inválido");
        }
        return Resultado.sucesso(JsonParser.parseString(json).getAsJsonObject());
    }

    private String lerJsonComoString(HttpServletRequest req) throws IOException {
        StringBuilder leitorJson = new StringBuilder();
        String linha;
        try (BufferedReader reader = req.getReader()) {
            while ((linha = reader.readLine()) != null) {
                leitorJson.append(linha);
            }
        }
        return leitorJson.toString();
    }

    private Resultado<ReservaEstoque> extrairReservaFiltro(HttpServletRequest req) {
        Produto produtoFiltro = new Produto();
        Marca marcaFiltro = new Marca();
        ReservaEstoque reservaFiltro = new ReservaEstoque();

        if(req.getParameter("idProduto") != null){
            produtoFiltro.setId(Integer.parseInt(req.getParameter("idProduto")));
        }

        if(req.getParameter("idMarca") != null){
            marcaFiltro.setId(Integer.parseInt(req.getParameter("idMarca")));
        }

        if(req.getParameter("id") != null){
            reservaFiltro.setId(Integer.parseInt(req.getParameter("id")));
        }

        if(req.getParameter("qtd") != null){
            reservaFiltro.setQuantidade(Integer.parseInt(req.getParameter("qtd")));
        }

        if(req.getParameter("status") != null){
            reservaFiltro.setStatus(Ativo.valueOf(req.getParameter("status")));
        }

        return Resultado.sucesso(reservaFiltro);
    }
}
