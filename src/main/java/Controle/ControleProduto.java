package Controle;

import Dominio.*;
import Fachada.Fachada;
import Fachada.IFachada;
import Util.Resultado;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import Enums.Genero;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.util.List;

@WebServlet(name = "ControleProduto", urlPatterns = "/controleProduto")
public class ControleProduto extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        Resultado<Produto> resultadoProdutoFiltro = extrairProdutoFiltro(req);

        IFachada fachada = new Fachada();
        Produto produtoFiltro = resultadoProdutoFiltro.getValor();
        Resultado<List<EntidadeDominio>> resultadoConsultaProduto = fachada.consultar(produtoFiltro);

        if (!resultadoConsultaProduto.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", resultadoConsultaProduto.getErro());
            out.print(gson.toJson(resposta));
            return;
        }

        String json = gson.toJson(resultadoConsultaProduto.getValor());
        resp.setStatus(HttpServletResponse.SC_OK);
        out.print(json);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        resp.setStatus(HttpServletResponse.SC_OK);
        out.print("O request funciona");
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
        if (!jsonObject.has("Produto")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes.");
            out.print(gson.toJson(resposta));
            return;
        }

        Produto produto = gson.fromJson(jsonObject.get("Produto"), Produto.class);
        IFachada fachada = new Fachada();
        Resultado<String> resultado = fachada.alterar(produto);

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
        Produto produto = new Produto();
        String idParam = req.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "ID do produto é obrigatório para exclusão.");
            out.print(gson.toJson(resposta));
            return;
        }

        try {
            produto.setId(Integer.parseInt(idParam));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "ID do produto é inválido.");
            out.print(gson.toJson(resposta));
            return;
        }

        Resultado<String> resultado = fachada.excluir(produto);

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

    private Resultado<Produto> extrairProdutoFiltro(HttpServletRequest req) {
        Produto produtoFiltro = new Produto();
        Marca marcaFiltro = new Marca();
        Categoria categoriaFiltro = new Categoria();

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
        if(req.getParameter("marca") != null){
            marcaFiltro.setNome(req.getParameter("marca"));
        }
        if(req.getParameter("categoria") != null){
            categoriaFiltro.setNome(req.getParameter("categoria"));
        }
        if(req.getParameter("ativo") != null){
            produtoFiltro.setAtivo(Boolean.valueOf(req.getParameter("ativo")));
        }else{
            produtoFiltro.setAtivo(true);
        }

        produtoFiltro.setMarca(marcaFiltro);
        produtoFiltro.setCategoria(categoriaFiltro);

        return Resultado.sucesso(produtoFiltro);
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

