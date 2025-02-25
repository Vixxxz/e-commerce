package Dao;

import Dominio.Cliente;
import Dominio.ClienteEndereco;
import Dominio.EntidadeDominio;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements IDAO {
    private Connection connection;

    public ClienteDAO(Connection connection) {
        this.connection = connection;
    }

    public ClienteDAO() {
    }

    public Resultado<Cliente> salvarClienteEEndereco(Cliente cliente, List<ClienteEndereco> enderecos) {
        try {
            Resultado<List<EntidadeDominio>> resultadoConsultaCliente = consultar(cliente);
            List<EntidadeDominio> clientes = resultadoConsultaCliente.getValor();

            if (!clientes.isEmpty()) {
                return Resultado.erro("cliente já existente");
            }

            Resultado<EntidadeDominio> resultadoSalvarCliente = salvar(cliente);
            Cliente clienteSalvo = (Cliente) resultadoSalvarCliente.getValor();
            ClienteEnderecoDAO clienteEnderecoDAO = new ClienteEnderecoDAO(connection);
            for (ClienteEndereco clienteEndereco : enderecos) {
                clienteEndereco.setCliente(clienteSalvo);
                clienteEnderecoDAO.salvaEnderecoCadastro(clienteEndereco);
            }
            System.out.println("Cliente e Endereço salvos com sucesso!");
            connection.commit();
            return Resultado.sucesso(clienteSalvo);
        } catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao salvar Cliente e Endereço: " + e.getMessage());
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
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        if (connection == null) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        Cliente cliente = (Cliente) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO cliente(cli_cpf, cli_email, cli_senha, cli_nome, ");
        sql.append("cli_genero, cli_dt_nasc, cli_tp_tel, cli_tel, cli_ranking, cli_dt_cadastro) ");
        sql.append("VALUES (?,?,?,?,?,?,?,?,?,?)");

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, cliente.getCpf());
            pst.setString(2, cliente.getEmail());
            pst.setString(3, cliente.getSenha());
            pst.setString(4, cliente.getNome());
            pst.setString(5, cliente.getGenero());
            pst.setDate(6, new Date(cliente.getDataNascimento().getTime()));
            pst.setString(7, cliente.getTipoTelefone());
            pst.setString(8, cliente.getTelefone());
            pst.setString(9, cliente.getRanking());
            pst.setTimestamp(10, new Timestamp(cliente.getDtCadastro().getTime()));
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir o cliente.");
                }
                int idCliente = rs.getInt(1);
                cliente.setId(idCliente);
            }
            return Resultado.sucesso(cliente);
        }
    }

    @Override
    public Resultado<EntidadeDominio> alterar(EntidadeDominio entidade) {
        Cliente cliente = (Cliente) entidade;
        try {
            if (connection == null) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE crud_v3.cliente SET ");
            sql.append("cli_nome = ?, cli_cpf = ?, cli_email = ?, cli_senha = ?, ");
            sql.append("cli_genero = ?, cli_dt_nasc = ?, cli_tp_tel = ?, cli_tel = ?, ");
            sql.append("cli_ranking = ?, cli_dt_cadastro = ? ");
            sql.append("WHERE cli_id = ?");


            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                pst.setString(1, cliente.getNome());
                pst.setString(2, cliente.getCpf());
                pst.setString(3, cliente.getEmail());
                pst.setString(4, cliente.getSenha());
                pst.setString(5, cliente.getGenero());
                pst.setDate(6, new Date(cliente.getDataNascimento().getTime()));
                pst.setString(7, cliente.getTipoTelefone());
                pst.setString(8, cliente.getTelefone());
                pst.setString(9, cliente.getRanking());
                pst.setTimestamp(10, new Timestamp(cliente.getDtCadastro().getTime()));
                pst.setInt(11, cliente.getId());

                int rowsUpdated = pst.executeUpdate();
                if (rowsUpdated == 0) {
                    return Resultado.erro("Nenhum cliente encontrado com o ID");
                }
            }
            connection.commit();
            return Resultado.sucesso(cliente);
        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao alterar Cliente: " + e.getMessage());
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
            if (connection == null) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            Cliente cliente = (Cliente) entidade;
            Resultado<List<EntidadeDominio>> resultadoClientes = consultar(cliente);
            List<EntidadeDominio> clientes = resultadoClientes.getValor();

            if(clientes.isEmpty()) {
                return Resultado.erro("Cliente não cadastrado no sistema");
            }

            excluirClienteEndereco(cliente);

            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM crud_v3.cliente c")
                    .append("WHERE c.cli_id = ? ");

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                pst.setInt(1, cliente.getId());
                int rowsDeleted = pst.executeUpdate();

                if (rowsDeleted == 0) {
                    return Resultado.erro("Nenhum cliente encontrado com o ID fornecido.");
                }
            }
            connection.commit();
            return Resultado.sucesso("Cliente excluido com sucesso");
        }catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao deletar Cliente: " + e.getMessage());
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

    private void excluirClienteEndereco(Cliente cliente) throws SQLException {
        ClienteEnderecoDAO clienteEnderecoDAO = new ClienteEnderecoDAO();
        ClienteEndereco clienteEndereco = new ClienteEndereco();
        clienteEndereco.setCliente(cliente);

        Resultado<List<EntidadeDominio>> resultadoClienteEndereco = clienteEnderecoDAO.consultar(clienteEndereco);
        List<EntidadeDominio> clienteEnderecos = resultadoClienteEndereco.getValor();

        if (!clienteEnderecos.isEmpty()) {
            for (EntidadeDominio cliEnd : clienteEnderecos) {
                clienteEnderecoDAO.excluir(cliEnd);
            }
        }
    }

    @Override
    public Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade) {
        try {
            if (connection == null) {
                connection = Conexao.getConnectionMySQL();
            }

            List<EntidadeDominio> clientes = new ArrayList<>();
            Cliente cliente = (Cliente) entidade;
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM crud_v3.cliente c ");
            sql.append("WHERE 1=1 ");

            if (cliente.getId() != null) {
                sql.append("AND c.cli_id = ? ");
                parametros.add(cliente.getId());
            }
            if (isStringValida(cliente.getRanking())) {
                sql.append("AND c.cli_ranking = ? ");
                parametros.add(cliente.getRanking());
            }
            if (isStringValida(cliente.getNome())) {
                sql.append("AND c.cli_nome = ? ");
                parametros.add(cliente.getNome());
            }
            if (isStringValida(cliente.getGenero())) {
                sql.append("AND c.cli_genero = ? ");
                parametros.add(cliente.getGenero());
            }
            if (isStringValida(cliente.getCpf())) {
                sql.append("AND c.cli_cpf = ? ");
                parametros.add(cliente.getCpf());
            }
            if (isStringValida(cliente.getTipoTelefone())) {
                sql.append("AND c.cli_tp_tel = ? ");
                parametros.add(cliente.getTipoTelefone());
            }
            if (isStringValida(cliente.getTelefone())) {
                sql.append("AND c.cli_tel = ? ");
                parametros.add(cliente.getTelefone());
            }
            if (isStringValida(cliente.getEmail())) {
                sql.append("AND c.cli_email = ? ");
                parametros.add(cliente.getEmail());
            }
            if (isStringValida(cliente.getSenha())) {
                sql.append("AND c.cli_senha = ? ");
                parametros.add(cliente.getSenha());
            }
            if (cliente.getDataNascimento() != null) {
                sql.append("AND c.cli_dt_nasc = ? ");
                parametros.add(new java.sql.Date(cliente.getDataNascimento().getTime()));
            }

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        clientes.add(mapeiaCliente(rs));
                    }
                }
            }
            return Resultado.sucesso(clientes);
        } catch (Exception e) {
            System.err.println("Erro ao consultar clientes: " + e.getMessage());
            return Resultado.erro("Erro ao consultar clientes: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }

    private boolean isStringValida(String value) {
        return value != null && !value.isBlank();
    }

    private Cliente mapeiaCliente(ResultSet rs) throws SQLException {
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
        return cli;
    }
}
