package Controle;

import Dominio.RequisicaoIA;
import Fachada.Fachada;
import Util.Resultado;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name= "ControleIA", urlPatterns = "/chatbot")
public class ControleIA extends HttpServlet {

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
        if (!jsonObject.has("requisicao")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes");
            out.print(gson.toJson(resposta));
            return;
        }

        // Cria a entidade RequisicaoIA com os dados fornecidos
        RequisicaoIA requisicao = gson.fromJson(jsonObject.get("requisicao"), RequisicaoIA.class);

        // Chama a fachada para processar a recomendação
        Fachada fachada = new Fachada();
        Resultado<String> resultado = fachada.processar(requisicao);

        if (!resultado.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", resultado.getErro());
            out.print(gson.toJson(resposta));
            return;
        }

        JsonObject resposta = new JsonObject();
        resposta.addProperty("resposta", resultado.getValor());
        out.print(gson.toJson(resposta));
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
