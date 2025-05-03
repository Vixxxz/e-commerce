package Controle;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import Dominio.*;
import Enums.Status;
import Fachada.*;
import Util.Resultado;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

@WebServlet(name = "ControleTrocaSolicitada", urlPatterns = "/controleTroca")
public class ControleTrocaSolicitada extends HttpServlet{
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
        if (!jsonObject.has("trocaSolicitada") || !jsonObject.has("trocaProdutos")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes");
            out.print(gson.toJson(resposta));
            return;
        }

        TrocaSolicitada trocaSolicitada = gson.fromJson(jsonObject.get("trocaSolicitada"), TrocaSolicitada.class);

        Type trocaProdutoListType = new TypeToken<List<TrocaSolicitadaTenis>>() {
        }.getType();
        List<TrocaSolicitadaTenis> trocaProdutos = gson.fromJson(jsonObject.get("trocaProdutos"), trocaProdutoListType);


        Fachada fachada = new Fachada();
        Resultado<String> resultado = fachada.salvarTroca(trocaSolicitada, trocaProdutos);

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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        Resultado<TrocaSolicitada> trocaSolicitadaFiltro = extrairTrocaFiltro(req);

        Fachada fachada = new Fachada();
        TrocaSolicitada trocaFiltro = trocaSolicitadaFiltro.getValor();
        Resultado<List<EntidadeDominio>> resultadoConsultaTroca = fachada.consultar(trocaFiltro);

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

    private Resultado<TrocaSolicitada> extrairTrocaFiltro(HttpServletRequest req) {
        Pedido pedidoFiltro = new Pedido();
        TrocaSolicitada trocaFiltro = new TrocaSolicitada();

        if(req.getParameter("idPedido") != null){
            pedidoFiltro.setId(Integer.parseInt(req.getParameter("idPedido")));
        }

        if(req.getParameter("statusPedido") != null){
            pedidoFiltro.setId(Integer.parseInt(req.getParameter("statusPedido")));
        }

        if(req.getParameter("status") != null){
            trocaFiltro.setStatus(Status.valueOf(req.getParameter("status")));
        }

        if (req.getParameter("statusList") != null) {
            List<Status> statusList = Arrays.stream(req.getParameter("statusList").split(","))
                    .map(String::trim)
                    .map(Status::valueOf)
                    .collect(Collectors.toList());
            pedidoFiltro.setListStatus(statusList);
        }

        return Resultado.sucesso(trocaFiltro);
    }
}
