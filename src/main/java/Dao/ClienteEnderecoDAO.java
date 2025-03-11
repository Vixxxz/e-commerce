package Dao;

import Dominio.*;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.*;

public class ClienteEnderecoDAO implements IDAO{
    private Connection connection;
    public ClienteEnderecoDAO(Connection connection) {
        this.connection = connection;
    }
    public ClienteEnderecoDAO() {}

    public void salvaEnderecoCadastro(ClienteEndereco clienteEndereco) throws SQLException, ClassNotFoundException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO cliente_endereco(cli_end_cli_id, cli_end_end_id, cli_end_num, ");
        sql.append("cli_end_tp_residencia, cli_end_tp_end, cli_end_obs, cli_end_dt_cadastro) ");
        sql.append("VALUES (?,?,?,?,?,?,?)");

        IDAO enderecoDAO = new EnderecoDAO(connection);

        Resultado<List<EntidadeDominio>> resultadoEnderecos = enderecoDAO.consultar(clienteEndereco.getEndereco());
        List<EntidadeDominio> enderecos = resultadoEnderecos.getValor();

        if (enderecos.isEmpty()) {
            Resultado<EntidadeDominio> resultadoClienteEndereco = enderecoDAO.salvar(clienteEndereco.getEndereco());
            clienteEndereco.setEndereco((Endereco) resultadoClienteEndereco.getValor());
        } else {
            clienteEndereco.setEndereco((Endereco) enderecos.getFirst());
        }
        clienteEndereco.complementarDtCadastro();
        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, clienteEndereco.getCliente().getId());
            pst.setInt(2, clienteEndereco.getEndereco().getId());
            pst.setString(3, clienteEndereco.getNumero());
            pst.setString(4, clienteEndereco.getTipoResidencia());
            pst.setString(5, clienteEndereco.getTipoEndereco());
            pst.setString(6, clienteEndereco.getObservacoes());
            pst.setTimestamp(7, new Timestamp(clienteEndereco.getDtCadastro().getTime()));

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir o ClienteEndereco.");
                }
                int idClienteEndereco = rs.getInt(1);
                clienteEndereco.setId(idClienteEndereco);
            }
        }
    }

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        try{
            ClienteEndereco clienteEndereco = (ClienteEndereco) entidade;

            Resultado<List<EntidadeDominio>> clientesEnderecos = consultar(clienteEndereco);
            if(!clientesEnderecos.isSucesso()){
                return Resultado.erro(clientesEnderecos.getErro());
            }
            List<EntidadeDominio> clientesEnderecosList = clientesEnderecos.getValor();
            if(clientesEnderecosList.contains(clienteEndereco)){
                return Resultado.erro("Cliente já possui endereço cadastrado.");
            }

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO cliente_endereco(cli_end_cli_id, cli_end_end_id, cli_end_num, ");
            sql.append("cli_end_tp_residencia, cli_end_tp_end, cli_end_obs, cli_end_dt_cadastro) ");
            sql.append("VALUES (?,?,?,?,?,?,?)");

            if(connection.isClosed() || connection == null){
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            IDAO enderecoDAO = new EnderecoDAO(connection);

            Resultado<List<EntidadeDominio>> resultadoEnderecos = enderecoDAO.consultar(clienteEndereco.getEndereco());
            List<EntidadeDominio> enderecos = resultadoEnderecos.getValor();

            if (enderecos.isEmpty()) {
                Resultado<EntidadeDominio> resultadoClienteEndereco = enderecoDAO.salvar(clienteEndereco.getEndereco());
                clienteEndereco.setEndereco((Endereco) resultadoClienteEndereco.getValor());
            } else {
                clienteEndereco.setEndereco((Endereco) enderecos.getFirst());
            }
            clienteEndereco.complementarDtCadastro();
            try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
                pst.setInt(1, clienteEndereco.getCliente().getId());
                pst.setInt(2, clienteEndereco.getEndereco().getId());
                pst.setString(3, clienteEndereco.getNumero());
                pst.setString(4, clienteEndereco.getTipoResidencia());
                pst.setString(5, clienteEndereco.getTipoEndereco());
                pst.setString(6, clienteEndereco.getObservacoes());
                pst.setTimestamp(7, new Timestamp(clienteEndereco.getDtCadastro().getTime()));

                pst.executeUpdate();

                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("Falha ao inserir o ClienteEndereco.");
                    }
                    int idClienteEndereco = rs.getInt(1);
                    clienteEndereco.setId(idClienteEndereco);
                }
            }
            connection.commit();
            return Resultado.sucesso(clienteEndereco);
        } catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao salvar ClienteEndereço: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Erro durante rollback: " + rollbackEx.getMessage());
                return Resultado.erro("Erro durante rollback: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }

    @Override
    public Resultado<EntidadeDominio> alterar(EntidadeDominio entidade) {
        try{
            ClienteEndereco clienteEndereco = (ClienteEndereco) entidade;

//            ClienteEndereco cliEnd = new ClienteEndereco();
//            cliEnd.setId(clienteEndereco.getId());
//            Resultado<List<EntidadeDominio>> resultadoCliEnd = consultar(cliEnd);
//            List<EntidadeDominio> listClienteEndereco = resultadoCliEnd.getValor();
//            cliEnd = (ClienteEndereco) listClienteEndereco.getFirst();
//            boolean isEnderecoAssociadoAOutrosClientes = isEnderecoAssociadoAOutrosClientes(cliEnd);

            if(connection.isClosed() || connection == null){
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE crud_v3.cliente_endereco SET ");
            sql.append("cli_end_cli_id = ?, cli_end_end_id = ?, cli_end_num = ?, ");
            sql.append("cli_end_tp_residencia = ?, cli_end_tp_end = ?, cli_end_obs = ? ");
            sql.append("WHERE cli_end_id = ?");

            IDAO enderecoDAO = new EnderecoDAO(connection);

            Resultado<List<EntidadeDominio>> resultadoEnderecos = enderecoDAO.consultar(clienteEndereco.getEndereco());
            List<EntidadeDominio> enderecos = resultadoEnderecos.getValor();

            if (enderecos.isEmpty()) {
                Resultado<EntidadeDominio> resultadoClienteEndereco = enderecoDAO.salvar(clienteEndereco.getEndereco());
                clienteEndereco.setEndereco((Endereco) resultadoClienteEndereco.getValor());
            } else {
                clienteEndereco.setEndereco((Endereco) enderecos.getFirst());
            }

            try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
                pst.setInt(1, clienteEndereco.getCliente().getId());
                pst.setInt(2, clienteEndereco.getEndereco().getId());
                pst.setString(3, clienteEndereco.getNumero());
                pst.setString(4, clienteEndereco.getTipoResidencia());
                pst.setString(5, clienteEndereco.getTipoEndereco());
                pst.setString(6, clienteEndereco.getObservacoes());
                pst.setInt(7, clienteEndereco.getId());

                pst.executeUpdate();

                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("Falha ao inserir o ClienteEndereco.");
                    }
                }
            }

//            if (!isEnderecoAssociadoAOutrosClientes) {
//                System.out.println("status da conexao: " + connection.isClosed());
//                enderecoDAO.excluir(cliEnd.getEndereco());
//            }

            connection.commit();
            return Resultado.sucesso(clienteEndereco);
        } catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao salvar ClienteEndereço: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Erro durante rollback: " + rollbackEx.getMessage());
                return Resultado.erro("Erro durante rollback: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }

    @Override
    public Resultado<String> excluir(EntidadeDominio entidade) {
        try{
            ClienteEndereco clienteEndereco = (ClienteEndereco) entidade;
            Resultado<List<EntidadeDominio>> resultadoClienteEnderecos = consultar(clienteEndereco);
            List<EntidadeDominio> clienteEnderecos = resultadoClienteEnderecos.getValor();

            if(clienteEnderecos.isEmpty()) {
                return Resultado.erro("ClienteEndereco não cadastrado no sistema");
            }

            boolean isEnderecoAssociadoAOutrosClientes = isEnderecoAssociadoAOutrosClientes(clienteEndereco);

            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM crud_v3.cliente_endereco ce WHERE ce.cli_end_id =?");

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                pst.setInt(1, clienteEndereco.getId());
                int rowsDeleted = pst.executeUpdate();

                if (rowsDeleted == 0) {
                    return Resultado.erro("Nenhum ClienteEndereco encontrado com o ID fornecido.");
                }
            }

            System.out.println("status da conexao: " + connection.isClosed());

            if (!isEnderecoAssociadoAOutrosClientes) {
                EnderecoDAO enderecoDAO = new EnderecoDAO(connection);
                System.out.println("status da conexao: " + connection.isClosed());
                enderecoDAO.excluir(clienteEndereco.getEndereco());
            }

            connection.commit();
            return Resultado.sucesso("ClienteEndereco excluido com sucesso");
        }catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao deletar ClienteEndereco: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Erro durante rollback: " + rollbackEx.getMessage());
                return Resultado.erro("Erro durante rollback: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }

    public Resultado<String> excluirClienteEEndereco(List<EntidadeDominio> entidades) {
        try {
            List<Endereco> enderecosParaExcluir = new ArrayList<>();

            for (EntidadeDominio cliEnd : entidades) {
                ClienteEndereco clienteEndereco = (ClienteEndereco) cliEnd;
                if (!isEnderecoAssociadoAOutrosClientes(clienteEndereco)) {
                    enderecosParaExcluir.add(clienteEndereco.getEndereco());
                }
            }

            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM crud_v3.cliente_endereco WHERE cli_end_id = ?");
            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (EntidadeDominio cliEnd : entidades) {
                    ClienteEndereco clienteEndereco = (ClienteEndereco) cliEnd;

                    pst.setInt(1, clienteEndereco.getId());
                    int rowsDeleted = pst.executeUpdate();

                    if (rowsDeleted == 0) {
                        return Resultado.erro("Nenhum ClienteEndereco encontrado com o ID fornecido.");
                    }
                    System.out.println("Cliente Endereco Excluido");
                }
            }

            if (!enderecosParaExcluir.isEmpty()) {
                EnderecoDAO enderecoDAO = new EnderecoDAO(connection);
                for (Endereco endereco : enderecosParaExcluir) {
                    enderecoDAO.excluir(endereco);
                }
            }

            connection.commit();
            return Resultado.sucesso("ClienteEndereco excluído com sucesso.");
        } catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao deletar ClienteEndereco: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Erro durante rollback: " + rollbackEx.getMessage());
                return Resultado.erro("Erro durante rollback: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }


    private boolean isEnderecoAssociadoAOutrosClientes(ClienteEndereco clienteEndereco) throws SQLException {
        ClienteEndereco cliEnd = new ClienteEndereco();
        cliEnd.setEndereco(clienteEndereco.getEndereco());

        Resultado<List<EntidadeDominio>> resultadoClienteEnderecos = consultar(cliEnd);
        List<EntidadeDominio> clienteEnderecos = resultadoClienteEnderecos.getValor();

        for (EntidadeDominio clienteEnd : clienteEnderecos) {
            ClienteEndereco cliEndereco = (ClienteEndereco) clienteEnd;
            if (!Objects.equals(clienteEndereco.getCliente().getId(), cliEndereco.getCliente().getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade) {
        try {
            if(connection == null || connection.isClosed()){
                connection = Conexao.getConnectionMySQL();
            }

            ClienteEndereco clienteEndereco = (ClienteEndereco) entidade;
            List<EntidadeDominio> clientesEnderecos = new ArrayList<>();
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = construirConsulta(clienteEndereco, parametros);

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        clientesEnderecos.add(mapeiaClienteEndereco(rs));
                    }
                }
            }
            return Resultado.sucesso(clientesEnderecos);
        } catch (Exception e) {
            System.err.println("Erro ao buscar Cliente Endereco: " + e.getMessage());
            return Resultado.erro("Erro ao buscar Cliente Endereco: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }

    private StringBuilder construirConsulta(ClienteEndereco clienteEndereco, List<Object> parametros) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * ")
                .append("FROM crud_v3.cliente_endereco ce ")
                .append("INNER JOIN crud_v3.cliente c           ON c.cli_id = ce.cli_end_cli_id ")
                .append("INNER JOIN crud_v3.endereco e          ON e.end_id = ce.cli_end_end_id ")
                .append("INNER JOIN crud_v3.bairro b            ON e.end_bai_id = b.bai_id ")
                .append("INNER JOIN crud_v3.cidade cd           ON b.bai_cid_id = cd.cid_id ")
                .append("INNER JOIN crud_v3.uf u                ON cd.cid_uf_id = u.uf_id ")
                .append("INNER JOIN crud_v3.pais p              ON u.uf_pai_id = p.pai_id ")
                .append("WHERE 1=1 ");

        adicionarCondicoesClienteEndereco(sql, clienteEndereco, parametros);

        return sql;
    }

    private void adicionarCondicoesClienteEndereco(StringBuilder sql, ClienteEndereco clienteEndereco, List<Object> parametros) {
        adicionarCondicao(sql, "ce.cli_end_id = ?", clienteEndereco.getId(), parametros);
        adicionarCondicao(sql, "ce.cli_end_num = ?", clienteEndereco.getNumero(), parametros, true);
        adicionarCondicao(sql, "ce.cli_end_tp_end = ?", clienteEndereco.getTipoEndereco(), parametros, true);
        adicionarCondicao(sql, "ce.cli_end_tp_residencia = ?", clienteEndereco.getTipoResidencia(), parametros, true);
        adicionarCondicao(sql, "ce.cli_end_obs = ?", clienteEndereco.getObservacoes(), parametros, true);

        if (clienteEndereco.getCliente() != null) {
            adicionarCondicoesCliente(sql, clienteEndereco.getCliente(), parametros);
        }

        if (clienteEndereco.getEndereco() != null) {
            adicionarCondicoesEndereco(sql, clienteEndereco.getEndereco(), parametros);
        }
    }

    private void adicionarCondicoesCliente(StringBuilder sql, Cliente cliente, List<Object> parametros) {
        adicionarCondicao(sql, "c.cli_id = ?", cliente.getId(), parametros);
        adicionarCondicao(sql, "c.cli_ranking = ?", cliente.getRanking(), parametros, true);
        adicionarCondicao(sql, "c.cli_nome = ?", cliente.getNome(), parametros, true);
        adicionarCondicao(sql, "c.cli_genero = ?", cliente.getGenero(), parametros, true);
        adicionarCondicao(sql, "c.cli_cpf = ?", cliente.getCpf(), parametros, true);
        adicionarCondicao(sql, "c.cli_tp_tel = ?", cliente.getTipoTelefone(), parametros, true);
        adicionarCondicao(sql, "c.cli_tel = ?", cliente.getTelefone(), parametros, true);
        adicionarCondicao(sql, "c.cli_email = ?", cliente.getEmail(), parametros, true);
        adicionarCondicao(sql, "c.cli_senha = ?", cliente.getSenha(), parametros, true);
        adicionarCondicao(sql, "c.cli_dt_nasc = ?", cliente.getDataNascimento(), parametros);
    }

    private void adicionarCondicoesEndereco(StringBuilder sql, Endereco endereco, List<Object> parametros) {
        adicionarCondicao(sql, "e.end_id = ?", endereco.getId(), parametros);
        adicionarCondicao(sql, "e.end_cep = ?", endereco.getCep(), parametros, true);
        adicionarCondicao(sql, "e.end_logradouro = ?", endereco.getLogradouro(), parametros, true);
        adicionarCondicao(sql, "e.end_tp_logradouro = ?", endereco.getTipoLogradouro(), parametros, true);
        if (endereco.getBairro() != null) {
            adicionarCondicaoBairro(sql, endereco.getBairro(), parametros);
        }
    }

    private void adicionarCondicaoBairro(StringBuilder sql, Bairro bairro, List<Object> parametros) {
        adicionarCondicao(sql, "b.bai_id = ?", bairro.getId(), parametros);
        adicionarCondicao(sql, "b.bai_nome = ?", bairro.getBairro(), parametros, true);

        if (bairro.getCidade() != null) {
            adicionarCondicaoCidade(sql, bairro.getCidade(), parametros);
        }
    }

    private void adicionarCondicaoCidade(StringBuilder sql, Cidade cidade, List<Object> parametros) {
        adicionarCondicao(sql, "cd.cid_id = ?", cidade.getId(), parametros);
        adicionarCondicao(sql, "cd.cid_nome = ?", cidade.getCidade(), parametros, true);

        if (cidade.getUf() != null) {
            adicionarCondicaoUf(sql, cidade.getUf(), parametros);
        }
    }

    private void adicionarCondicaoUf(StringBuilder sql, Uf uf, List<Object> parametros) {
        adicionarCondicao(sql, "u.uf_id = ?", uf.getId(), parametros);
        adicionarCondicao(sql, "u.uf_nome = ?", uf.getUf(), parametros, true);

        if (uf.getPais() != null) {
            adicionarCondicaoPais(sql, uf.getPais(), parametros);
        }
    }

    private void adicionarCondicaoPais(StringBuilder sql, Pais pais, List<Object> parametros) {
        adicionarCondicao(sql, "p.pai_id = ?", pais.getId(), parametros);
        adicionarCondicao(sql, "p.pai_nome = ?", pais.getPais(), parametros, true);
    }

    private void adicionarCondicao(StringBuilder sql, String condicao, Object valor, List<Object> parametros) {
        adicionarCondicao(sql, condicao, valor, parametros, false);
    }

    private void adicionarCondicao(StringBuilder sql, String condicao, Object valor, List<Object> parametros, boolean isString) {
        if ((isString && isStringValida((String) valor)) || (!isString && valor != null)) {
            sql.append(" AND ").append(condicao).append(" ");
            parametros.add(valor);
        }
    }

    private boolean isStringValida(String value) {
        return value != null && !value.isBlank();
    }

    private ClienteEndereco mapeiaClienteEndereco(ResultSet rs) throws SQLException {
        ClienteEndereco ce = new ClienteEndereco();
        ce.setId(rs.getInt("cli_end_id"));
        ce.setNumero(rs.getString("cli_end_num"));
        ce.setTipoResidencia(rs.getString("cli_end_tp_residencia"));
        ce.setTipoEndereco(rs.getString("cli_end_tp_end"));
        ce.setObservacoes(rs.getString("cli_end_obs"));

        Endereco end = new Endereco();
        end.setId(rs.getInt("end_id"));
        end.setCep(rs.getString("end_cep"));
        end.setLogradouro(rs.getString("end_logradouro"));
        end.setTipoLogradouro(rs.getString("end_tp_logradouro"));

        Cliente cli = new Cliente();
        cli.setId(rs.getInt("cli_id"));
        cli.setRanking(rs.getString("cli_ranking"));
        cli.setNome(rs.getString("cli_nome"));
        cli.setGenero(rs.getString("cli_genero"));
        cli.setCpf(rs.getString("cli_cpf"));
        cli.setTipoTelefone(rs.getString("cli_tp_tel"));
        cli.setTelefone(rs.getString("cli_tel"));
        cli.setEmail(rs.getString("cli_email"));
        cli.setSenha(rs.getString("cli_senha"));
        cli.setDataNascimento(rs.getDate("cli_dt_nasc"));

        Bairro bai = new Bairro();
        bai.setId(rs.getInt("bai_id"));
        bai.setBairro(rs.getString("bai_nome"));

        Cidade cidade = new Cidade();
        cidade.setId(rs.getInt("cid_id"));
        cidade.setCidade(rs.getString("cid_nome"));

        Uf uf = new Uf();
        uf.setId(rs.getInt("uf_id"));
        uf.setUf(rs.getString("uf_nome"));

        Pais pais = new Pais();
        pais.setId(rs.getInt("pai_id"));
        pais.setPais(rs.getString("pai_nome"));

        uf.setPais(pais);
        cidade.setUf(uf);
        bai.setCidade(cidade);
        end.setBairro(bai);

        ce.setCliente(cli);
        ce.setEndereco(end);

        return ce;
    }
}
