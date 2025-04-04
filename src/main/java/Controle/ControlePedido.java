package Controle;

import Dominio.Cliente;
import Dominio.ClienteEndereco;
import Dominio.EntidadeDominio;
import Dominio.Pedido;
import Fachada.Fachada;
import Fachada.IFachada;
import Util.ConversorData;
import Util.Resultado;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

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
        if (!jsonObject.has("Cliente") || !jsonObject.has("ClienteEndereco")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes");
            out.print(gson.toJson(resposta));
            return;
        }

        Cliente cliente = gson.fromJson(jsonObject.get("Cliente"), Cliente.class);
        Type clienteEnderecoListType = new TypeToken<List<ClienteEndereco>>() {
        }.getType();
        List<ClienteEndereco> clienteEnderecos = gson.fromJson(jsonObject.get("ClienteEndereco"), clienteEnderecoListType);
        Fachada fachada = new Fachada();

        Resultado<String> resultado = fachada.salvarClienteEEndereco(cliente, clienteEnderecos);

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
        if (!jsonObject.has("Cliente")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "{\"erro\": \"JSON inválido: Campos obrigatórios ausentes.\"}");
            out.print(gson.toJson(resposta));
            return;
        }

        Cliente cliente = gson.fromJson(jsonObject.get("Cliente"), Cliente.class);

        IFachada fachada = new Fachada();
        Resultado<String> resultado = fachada.alterar(cliente);

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
        Cliente clienteFiltro = new Cliente();
        String idParam = req.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "ID do cliente é obrigatório para exclusão.");
            out.print(gson.toJson(resposta));
            return;
        }

        try {
            clienteFiltro.setId(Integer.parseInt(idParam));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "ID do cliente inválido.");
            out.print(gson.toJson(resposta));
            return;
        }

        Resultado<String> resultado = fachada.excluir(clienteFiltro);

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

    //todo: arrumar a função
    private Resultado<Pedido> extrairPedidoFiltro(HttpServletRequest req) {
        Cliente clienteFiltro = new Cliente();
        if (req.getParameter("id") != null) {
            clienteFiltro.setId(Integer.parseInt(req.getParameter("id")));
        }
        if (req.getParameter("ranking") != null) {
            clienteFiltro.setRanking(req.getParameter("ranking"));
        }
        if (req.getParameter("nome") != null) {
            clienteFiltro.setNome(req.getParameter("nome"));
        }
        if (req.getParameter("genero") != null) {
            clienteFiltro.setGenero(req.getParameter("genero"));
        }
        if (req.getParameter("cpf") != null) {
            clienteFiltro.setCpf(req.getParameter("cpf"));
        }
        if (req.getParameter("tipoTelefone") != null) {
            clienteFiltro.setTipoTelefone(req.getParameter("tipoTelefone"));
        }
        if (req.getParameter("telefone") != null) {
            clienteFiltro.setTelefone(req.getParameter("telefone"));
        }
        if (req.getParameter("email") != null) {
            clienteFiltro.setEmail(req.getParameter("email"));
        }
        if (req.getParameter("senha") != null) {
            clienteFiltro.setSenha(req.getParameter("senha"));
        }
        if (req.getParameter("dataNascimento") != null) {
            String dataStr = req.getParameter("dataNascimento");

            Resultado<Date> resultadoData = ConversorData.converter(dataStr);

            if (!resultadoData.isSucesso()) {
                return Resultado.erro(resultadoData.getErro());
            }
            clienteFiltro.setDataNascimento(resultadoData.getValor());
        }
        return Resultado.sucesso(null);
    }
}
