package controle;

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

@WebServlet(name = "ControleEndereco", urlPatterns = "/controleendereco")
public class ControleEndereco extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();

        Resultado<ClienteEndereco> resultadoClienteFiltro = extrairEnderecoFiltro(req);

        if (!resultadoClienteFiltro.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", resultadoClienteFiltro.getErro());
            out.print(gson.toJson(resposta));
            return;
        }

        IFachada fachada = new Fachada();
        ClienteEndereco clienteEnderecoFiltro = resultadoClienteFiltro.getValor();
        Resultado<List<EntidadeDominio>> resultadoConsultaClienteEndereco = fachada.consultar(clienteEnderecoFiltro);

        if (!resultadoConsultaClienteEndereco.isSucesso()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", resultadoConsultaClienteEndereco.getErro());
            out.print(gson.toJson(resposta));
            return;
        }

        String json = gson.toJson(resultadoConsultaClienteEndereco.getValor());
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
        if (!jsonObject.has("ClienteEndereco")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes");
            out.print(gson.toJson(resposta));
            return;
        }

        ClienteEndereco clienteEndereco = gson.fromJson(jsonObject.get("ClienteEndereco"), ClienteEndereco.class);
        System.out.println(clienteEndereco.getCliente().getId());
        Fachada fachada = new Fachada();

        Resultado<String> resultado = fachada.salvar(clienteEndereco);

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
        if (!jsonObject.has("ClienteEndereco")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "JSON inválido: Campos obrigatórios ausentes.");
            out.print(gson.toJson(resposta));
            return;
        }

        ClienteEndereco clienteEndereco = gson.fromJson(jsonObject.get("ClienteEndereco"), ClienteEndereco.class);

        IFachada fachada = new Fachada();
        Resultado<String> resultado = fachada.alterar(clienteEndereco);

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
        ClienteEndereco clienteEnderecoFiltro = new ClienteEndereco();
        String idParam = req.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "ID do cliente endereco é obrigatório para exclusão.");
            out.print(gson.toJson(resposta));
            return;
        }

        try {
            clienteEnderecoFiltro.setId(Integer.parseInt(idParam));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject resposta = new JsonObject();
            resposta.addProperty("erro", "ID do cliente endereco inválido.");
            out.print(gson.toJson(resposta));
            return;
        }

        Resultado<String> resultado = fachada.excluir(clienteEnderecoFiltro);

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

    private Resultado<ClienteEndereco> extrairEnderecoFiltro(HttpServletRequest req) {
        ClienteEndereco clienteEnderecoFiltro = new ClienteEndereco();
        Endereco enderecoFiltro = new Endereco();
        Bairro bairroFiltro = new Bairro();
        Cidade cidadeFiltro = new Cidade();
        Uf ufFiltro = new Uf();
        Pais paisFiltro = new Pais();
        Cliente clienteFiltro = new Cliente();

        if(req.getParameter("idCliente") != null){
            clienteFiltro.setId(Integer.parseInt(req.getParameter("idCliente")));
        }

        if (req.getParameter("idClienteEndereco") != null) {
            clienteEnderecoFiltro.setId(Integer.parseInt(req.getParameter("idClienteEndereco")));
        }
        if (req.getParameter("numero") != null) {
            clienteEnderecoFiltro.setNumero(req.getParameter("numero"));
        }
        if (req.getParameter("tipoResidencia") != null) {
            clienteEnderecoFiltro.setTipoEndereco(req.getParameter("tipoResidencia"));
        }
        if (req.getParameter("obs") != null) {
            clienteEnderecoFiltro.setObservacoes(req.getParameter("obs"));
        }

        if (req.getParameter("idEndereco") != null) {
            enderecoFiltro.setId(Integer.valueOf(req.getParameter("idEndereco")));
        }
        if (req.getParameter("cep") != null) {
            enderecoFiltro.setCep(req.getParameter("cep"));
        }
        if (req.getParameter("logradouro") != null) {
            enderecoFiltro.setLogradouro(req.getParameter("logradouro"));
        }
        if (req.getParameter("tipoLogradouro") != null) {
            enderecoFiltro.setTipoLogradouro(req.getParameter("tipoLogradouro"));
        }

        if (req.getParameter("idBairro") != null) {
            bairroFiltro.setId(Integer.valueOf(req.getParameter("idBairro")));
        }
        if (req.getParameter("bairro") != null) {
            bairroFiltro.setBairro(req.getParameter("bairro"));
        }

        if (req.getParameter("idCidade") != null) {
            cidadeFiltro.setId(Integer.valueOf(req.getParameter("idCidade")));
        }
        if (req.getParameter("cidade") != null) {
            cidadeFiltro.setCidade((req.getParameter("cidade")));
        }

        if (req.getParameter("idUf") != null) {
            ufFiltro.setId(Integer.valueOf(req.getParameter("idUf")));
        }
        if (req.getParameter("uf") != null) {
            ufFiltro.setUf((req.getParameter("uf")));
        }

        if (req.getParameter("idPais") != null) {
            paisFiltro.setId(Integer.valueOf(req.getParameter("idPais")));
        }
        if (req.getParameter("pais") != null) {
            paisFiltro.setPais((req.getParameter("pais")));
        }

        ufFiltro.setPais(paisFiltro);
        cidadeFiltro.setUf(ufFiltro);
        bairroFiltro.setCidade(cidadeFiltro);
        enderecoFiltro.setBairro(bairroFiltro);
        clienteEnderecoFiltro.setEndereco(enderecoFiltro);
        clienteEnderecoFiltro.setCliente(clienteFiltro);

        return Resultado.sucesso(clienteEnderecoFiltro);
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
