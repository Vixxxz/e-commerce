package Controle;

import Dominio.*;
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

@WebServlet(name = "ControleCartao", urlPatterns = "/controleCartao")
public class ControleCartao extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        Resultado<Cartao> resultadoCartaoFiltro = extrairCartaoFiltro(req);

        IFachada fachada = new Fachada();
        Cartao cartaoFiltro = resultadoCartaoFiltro.getValor();
        Resultado<List<EntidadeDominio>> resultadoConsultaartao = fachada.consultar(cartaoFiltro);

        if (!resultadoConsultaartao.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", resultadoConsultaartao.getErro());
            out.print(gson.toJson(resposta));
            return;
        }

        String json = gson.toJson(resultadoConsultaartao.getValor());
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
        if (!jsonObject.has("Cartao")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes");
            out.print(gson.toJson(resposta));
            return;
        }

        Cartao cartao = gson.fromJson(jsonObject.get("Cartao"), Cartao.class);
        Fachada fachada = new Fachada();
        Resultado<String> resultado = fachada.salvar(cartao);

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
        System.out.println(jsonObject.toString());
        if (!jsonObject.has("Cartao")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes.");
            out.print(gson.toJson(resposta));
            return;
        }

        Cartao cartao = gson.fromJson(jsonObject.get("Cartao"), Cartao.class);
        IFachada fachada = new Fachada();
        Resultado<String> resultado = fachada.alterar(cartao);

        if(!resultado.isSucesso()) {
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
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        IFachada fachada = new Fachada();
        Cartao cartao = new Cartao();
        String idParam = req.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "ID do cartao é obrigatório para exclusão.");
            out.print(gson.toJson(resposta));
            return;
        }

        try {
            cartao.setId(Integer.parseInt(idParam));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "ID do cartao endereco inválido.");
            out.print(gson.toJson(resposta));
            return;
        }

        Resultado<String> resultado = fachada.excluir(cartao);

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

    private Resultado<Cartao> extrairCartaoFiltro(HttpServletRequest req) {
        Cartao cartaoFiltro = new Cartao();
        Bandeira bandeiraFiltro = new Bandeira();
        Cliente clienteFiltro = new Cliente();

        if(req.getParameter("id") != null){
            cartaoFiltro.setId(Integer.parseInt(req.getParameter("id")));
        }

        if (req.getParameter("numero") != null) {
            cartaoFiltro.setNumero((req.getParameter("numero")));
        }
        if(req.getParameter("numSeguranca") != null) {
            cartaoFiltro.setNumSeguranca((req.getParameter("numSeguranca")));
        }
        if(req.getParameter("nomeImpresso") != null) {
            cartaoFiltro.setNomeImpresso((req.getParameter("nomeImpresso")));
        }
        if(req.getParameter("preferencial") != null){
            cartaoFiltro.setPreferencial(Boolean.parseBoolean(req.getParameter("preferencial")));
        }

        if(req.getParameter("idBandeira") != null){
            bandeiraFiltro.setId(Integer.parseInt(req.getParameter("idBandeira")));
        }
        if(req.getParameter("nomeBandeira") != null){
            bandeiraFiltro.setNomeBandeira(req.getParameter("nomeBandeira"));
        }

        if(req.getParameter("idCliente") != null){
            clienteFiltro.setId(Integer.parseInt(req.getParameter("idCliente")));
        }
        if(req.getParameter("cpf") != null){
            clienteFiltro.setCpf(req.getParameter("cpf"));
        }

        cartaoFiltro.setBandeira(bandeiraFiltro);
        cartaoFiltro.setCliente(clienteFiltro);

        return Resultado.sucesso(cartaoFiltro);
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
}
