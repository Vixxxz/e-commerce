package Controle;

import Dominio.*;
import Enums.Genero;
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

@WebServlet(name = "ControleEstoque", urlPatterns = "/controleEstoque")
public class ControleEstoque extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        Resultado<Estoque> resultadoEstoqueFiltro = extrairEstoqueFiltro(req);

        if (!resultadoEstoqueFiltro.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "{" + resultadoEstoqueFiltro.getErro() + "}");
            out.print(gson.toJson(resposta));
            return;
        }

        IFachada fachada = new Fachada();
        Estoque estoqueFiltro = resultadoEstoqueFiltro.getValor();
        Resultado<List<EntidadeDominio>> resultadoConsultaPedido = fachada.consultar(estoqueFiltro);

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
//        if (!jsonObject.has("Cliente") || !jsonObject.has("ClienteEndereco")) {
//            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            JsonObject resposta = new JsonObject();
//            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes");
//            out.print(gson.toJson(resposta));
//            return;
//        }
//
//        Cliente cliente = gson.fromJson(jsonObject.get("Cliente"), Cliente.class);
//        Type clienteEnderecoListType = new TypeToken<List<ClienteEndereco>>() {
//        }.getType();
//        List<ClienteEndereco> clienteEnderecos = gson.fromJson(jsonObject.get("ClienteEndereco"), clienteEnderecoListType);
//        Fachada fachada = new Fachada();
//
//        Resultado<String> resultado = fachada.salvarClienteEEndereco(cliente, clienteEnderecos);
//
//        if (!resultado.isSucesso()) {
//            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            JsonObject resposta = new JsonObject();
//            resposta.addProperty("erro", resultado.getErro());
//            out.print(gson.toJson(resposta));
//            return;
//        }
//        String json = gson.toJson(resultado.getValor());
//        resp.setStatus(HttpServletResponse.SC_OK);
//        out.print(json);
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

    private Resultado<Estoque> extrairEstoqueFiltro(HttpServletRequest req) {
        Produto produtoFiltro = new Produto();

        if(req.getParameter("id") != null){
            produtoFiltro.setId(Integer.parseInt(req.getParameter("id")));
        }
        if (req.getParameter("sku") != null) {
            produtoFiltro.setSku((req.getParameter("sku")));
        }
        if(req.getParameter("nome") != null) {
            produtoFiltro.setNome((req.getParameter("nome")));
        }
        if(req.getParameter("preco") != null) {
            produtoFiltro.setPreco(Double.valueOf((req.getParameter("preco"))));
        }
        if(req.getParameter("modelo") != null){
            produtoFiltro.setModelo(req.getParameter("modelo"));
        }
        if(req.getParameter("cor") != null){
            produtoFiltro.setCor(req.getParameter("cor"));
        }
        if(req.getParameter("tamanho") != null){
            produtoFiltro.setTamanho(Integer.valueOf(req.getParameter("tamaho")));
        }
        if(req.getParameter("genero") != null){
            produtoFiltro.setGenero(Genero.valueOf(req.getParameter("genero")));
        }
        if(req.getParameter("ativo") != null){
            produtoFiltro.setAtivo(Boolean.valueOf(req.getParameter("ativo")));
        }else{
            produtoFiltro.setAtivo(true);
        }

        Estoque estFiltro = new Estoque();
        if(req.getParameter("quantidade") != null){
            estFiltro.setQuantidade(Integer.valueOf(req.getParameter("quantidade")));
        }
        if(req.getParameter("valorCusto") != null){
            estFiltro.setValorCusto(Double.valueOf(req.getParameter("valorCusto")));
        }

        estFiltro.setProduto(produtoFiltro);

        return Resultado.sucesso(estFiltro);
    }
}
