package Controle;

import Dominio.*;
import Enums.TipoCupom;
import Fachada.Fachada;
import Fachada.IFachada;
import Util.Resultado;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.util.List;

@WebServlet(name = "ControleCupom", urlPatterns = "/controleCupom")
public class ControleCupom extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        Resultado<Cupom> resultadoCupomFiltro = extrairCupomFiltro(req);

        if (!resultadoCupomFiltro.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "{" + resultadoCupomFiltro.getErro() + "}");
            out.print(gson.toJson(resposta));
            return;
        }

        IFachada fachada = new Fachada();
        Cupom cupomFiltro = resultadoCupomFiltro.getValor();
        Resultado<List<EntidadeDominio>> resultadoConsultaCupom = fachada.consultar(cupomFiltro);

        if (!resultadoConsultaCupom.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "{\"erro\": \"" + resultadoConsultaCupom.getErro() + "\"}");
            out.print(gson.toJson(resposta));
            return;
        }

        String json = gson.toJson(resultadoConsultaCupom.getValor());
        resp.setStatus(HttpServletResponse.SC_OK);
        out.print(json);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

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
        if (!jsonObject.has("Cupom")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes");
            out.print(gson.toJson(resposta));
            return;
        }

        Cupom cupom = gson.fromJson(jsonObject.get("Cupom"), Cupom.class);
        Fachada fachada = new Fachada();

        Resultado<String> resultado = fachada.salvar(cupom);

        if (!resultado.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", resultado.getErro());
            out.print(gson.toJson(resposta));
            return;
        }
        String json = gson.toJson(resultado.getValor());
        resp.setStatus(HttpServletResponse.SC_OK);
        out.print(json);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        req.setCharacterEncoding("UTF-8");
//        resp.setContentType("application/json");
//        resp.setCharacterEncoding("UTF-8");
//        PrintWriter out = resp.getWriter();
//
//        Gson gson = new Gson();
//        Resultado<JsonObject> ResultJsonObject = lerJsonComoObjeto(req);
//
//        if (!ResultJsonObject.isSucesso()) {
//            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            JsonObject resposta = new JsonObject();
//            resposta.addProperty("erro", ResultJsonObject.getErro());
//            out.print(gson.toJson(resposta));
//            return;
//        }
//
//        JsonObject jsonObject = ResultJsonObject.getValor();
//        if (!jsonObject.has("Cliente")) {
//            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            JsonObject resposta = new JsonObject();
//            resposta.addProperty("erro", "{\"erro\": \"JSON inválido: Campos obrigatórios ausentes.\"}");
//            out.print(gson.toJson(resposta));
//            return;
//        }
//
//        Cliente cliente = gson.fromJson(jsonObject.get("Cliente"), Cliente.class);
//
//        IFachada fachada = new Fachada();
//        Resultado<String> resultado = fachada.alterar(cliente);
//
//        if (!resultado.isSucesso()) {
//            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            JsonObject resposta = new JsonObject();
//            resposta.addProperty("erro", "{\"erro\": \"" + resultado.getErro() + "\"}");
//            out.print(gson.toJson(resposta));
//            return;
//        }
//        String json = gson.toJson(resultado.getValor());
//        resp.setStatus(HttpServletResponse.SC_OK);
//        out.print(json);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        req.setCharacterEncoding("UTF-8");
//        resp.setContentType("application/json");
//        resp.setCharacterEncoding("UTF-8");
//        PrintWriter out = resp.getWriter();
//        Gson gson = new Gson();
//
//        IFachada fachada = new Fachada();
//        Cliente clienteFiltro = new Cliente();
//        String idParam = req.getParameter("id");
//
//        if (idParam == null || idParam.isEmpty()) {
//            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            JsonObject resposta = new JsonObject();
//            resposta.addProperty("erro", "ID do cliente é obrigatório para exclusão.");
//            out.print(gson.toJson(resposta));
//            return;
//        }
//
//        try {
//            clienteFiltro.setId(Integer.parseInt(idParam));
//        } catch (NumberFormatException e) {
//            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            JsonObject resposta = new JsonObject();
//            resposta.addProperty("erro", "ID do cliente inválido.");
//            out.print(gson.toJson(resposta));
//            return;
//        }
//
//        Resultado<String> resultado = fachada.excluir(clienteFiltro);
//
//        if (!resultado.isSucesso()) {
//            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            JsonObject resposta = new JsonObject();
//            resposta.addProperty("erro", resultado.getErro());
//            out.print(gson.toJson(resposta));
//            return;
//        }
//
//        String json = gson.toJson(resultado.getValor());
//        resp.setStatus(HttpServletResponse.SC_OK);
//        out.print(json);
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

    private Resultado<Cupom> extrairCupomFiltro(HttpServletRequest req) {
        Cupom cupomFiltro = new Cupom();
        Cliente clienteFiltro = new Cliente();

        if (req.getParameter("id") != null) {
            cupomFiltro.setId(Integer.parseInt(req.getParameter("id")));
        }
        if (req.getParameter("codigo") != null) {
            cupomFiltro.setCodigo(req.getParameter("codigo"));
        }
        if (req.getParameter("tipo") != null) {
            cupomFiltro.setTipo(TipoCupom.valueOf(req.getParameter("tipo")));
        }
        if(req.getParameter("idCliente") != null){
            clienteFiltro.setId(Integer.parseInt(req.getParameter("idCliente")));
        }
        if(req.getParameter("cpf") != null){
            clienteFiltro.setCpf(req.getParameter("cpf"));
        }

        cupomFiltro.setCliente(clienteFiltro);

        return Resultado.sucesso(cupomFiltro);
    }
}
