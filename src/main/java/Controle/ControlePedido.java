package Controle;

import Dominio.*;
import Enums.Status;
import Fachada.Fachada;
import Fachada.IFachada;
import Util.Resultado;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "ControlePedido", urlPatterns = "/controlePedido")
public class ControlePedido extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        Resultado<Pedido> resultadoPedidoFiltro = extrairPedidoFiltro(req);

        if (!resultadoPedidoFiltro.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "{" + resultadoPedidoFiltro.getErro() + "}");
            out.print(gson.toJson(resposta));
            return;
        }

        IFachada fachada = new Fachada();
        Pedido pedidoFiltro = resultadoPedidoFiltro.getValor();
        Resultado<List<EntidadeDominio>> resultadoConsultaPedido = fachada.consultar(pedidoFiltro);

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
        if (!jsonObject.has("pedido") || !jsonObject.has("PedidoProdutos") || !jsonObject.has("CartaoPedido")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes");
            out.print(gson.toJson(resposta));
            return;
        }

        Pedido pedido = gson.fromJson(jsonObject.get("pedido"), Pedido.class);

        Type pedidoProdutoListType = new TypeToken<List<PedidoProduto>>() {
        }.getType();
        List<PedidoProduto> pedidoProdutos = gson.fromJson(jsonObject.get("PedidoProdutos"), pedidoProdutoListType);

        Type cartaoPedidoListType = new TypeToken<List<CartaoPedido>>() {
        }.getType();
        List<CartaoPedido> cartaoPedidos = gson.fromJson(jsonObject.get("CartaoPedido"), cartaoPedidoListType);

        List<Cupom>cupons = null;
        if(jsonObject.has("cupons")){
            Type cupomListType = new TypeToken<List<Cupom>>(){}.getType();
            cupons = gson.fromJson(jsonObject.get("cupons"), cupomListType);
        }

        ReservaEstoque reservaEstoque = new ReservaEstoque();
        reservaEstoque.setSessao(sessaoId);

        Fachada fachada = new Fachada();
        Resultado<String> resultado = fachada.salvarPedidoProduto(pedido, pedidoProdutos, cartaoPedidos, reservaEstoque, cupons);

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
        if (!jsonObject.has("Pedido")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "{\"erro\": \"JSON inválido: Campos obrigatórios ausentes.\"}");
            out.print(gson.toJson(resposta));
            return;
        }

        Pedido pedido = gson.fromJson(jsonObject.get("Pedido"), Pedido.class);

        IFachada fachada = new Fachada();
        Resultado<String> resultado = fachada.alterar(pedido);

        if (!resultado.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "{\"erro\": \"" + resultado.getErro() + "\"}");
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
        Pedido pedidoFiltro = new Pedido();
        String idParam = req.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "ID do pedido é obrigatório para exclusão.");
            out.print(gson.toJson(resposta));
            return;
        }

        try {
            pedidoFiltro.setId(Integer.parseInt(idParam));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "ID do pedido inválido.");
            out.print(gson.toJson(resposta));
            return;
        }

        Resultado<String> resultado = fachada.excluir(pedidoFiltro);

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

    private Resultado<Pedido> extrairPedidoFiltro(HttpServletRequest req) {
        Pedido pedido = new Pedido();
        ClienteEndereco clienteEndereco = new ClienteEndereco();
        Cliente cliente = new Cliente();

        if(req.getParameter("id") != null){
            pedido.setId(Integer.parseInt(req.getParameter("id")));
        }

        if(req.getParameter("idCliente") != null){
            cliente.setId(Integer.parseInt(req.getParameter("idCliente")));
        }

        if(req.getParameter("cpf") != null){
            cliente.setCpf(req.getParameter("cpf"));
        }

        if(req.getParameter("status") != null){
            pedido.setStatus(Status.valueOf(req.getParameter("status")));
        }


        if (req.getParameter("statusList") != null) {
            List<Status> statusList = Arrays.stream(req.getParameter("statusList").split(","))
                    .map(String::trim)
                    .map(Status::valueOf)
                    .collect(Collectors.toList());
            pedido.setListStatus(statusList);
        }

        clienteEndereco.setCliente(cliente);
        pedido.setClienteEndereco(clienteEndereco);

        return Resultado.sucesso(pedido);
    }
}
