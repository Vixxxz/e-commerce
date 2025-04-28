package Dao;

import Dominio.*;
import Enums.Genero;
import Enums.Status;
import Enums.TipoCupom;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PedidoDAO implements IDAO{
    private Connection connection;

    public PedidoDAO(Connection connection) {
        this.connection = connection;
    }

    public PedidoDAO() {
    }

    //todo: fazer a movimentacao do estoque
    public Resultado<Pedido> salvarPedidoEProduto(Pedido pedido, List<PedidoProduto> pedidoProdutos, List<CartaoPedido> cartaoPedidos) {
        try {
            Resultado<EntidadeDominio> resultadoSalvarPedido = salvar(pedido);
            Pedido pedidoSalvo = (Pedido) resultadoSalvarPedido.getValor();
            PedidoProdutoDAO pedidoProdutoDAO = new PedidoProdutoDAO(connection);
            for (PedidoProduto pedidoProduto : pedidoProdutos) {
                pedidoProduto.setPedido(pedidoSalvo);
                Resultado<EntidadeDominio> resultadoPedidoProduto = pedidoProdutoDAO.salvar(pedidoProduto);
                if(!resultadoPedidoProduto.isSucesso()){
                    return Resultado.erro("Erro ao salvar pedido de produto: " + resultadoPedidoProduto.getErro());
                }
            }
            CartaoPedidoDAO cartaoPedidoDAO = new CartaoPedidoDAO(connection);
            for(CartaoPedido cartaoPedido : cartaoPedidos) {
                cartaoPedido.setPedido(pedidoSalvo);
                Resultado<EntidadeDominio> resultadoCartaoPedido = cartaoPedidoDAO.salvar(cartaoPedido);
                if(!resultadoCartaoPedido.isSucesso()) {
                    return Resultado.erro("Erro ao salvar cartão de pedido: " + resultadoCartaoPedido.getErro());
                }
            }
            EstoqueDAO estoqueDAO = new EstoqueDAO(connection);
            for (PedidoProduto pedidoProduto : pedidoProdutos) {
                Resultado<EntidadeDominio>resultadoAtualizaEstoque = estoqueDAO.atualizarEstoque(pedidoProduto);
                if(!resultadoAtualizaEstoque.isSucesso()) {
                    return Resultado.erro("Erro ao atualizar estoque: " + resultadoAtualizaEstoque.getErro());
                }
            }
            System.out.println("Pedido salvo com sucesso!");
            connection.commit();
            return Resultado.sucesso(pedidoSalvo);
        } catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao salvar pedido: " + e.getMessage());
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
        if (connection == null || connection.isClosed()) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        Pedido pedido = (Pedido) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO pedido(ped_valor_total, ped_status, ped_dt_cadastro, ped_fre_id, ");
        sql.append("ped_cli_end_id, ped_cli_end_cli_id, ped_cli_end_end_id) ");
        sql.append("VALUES (?,?,?,?,?,?,?)");

        pedido.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setDouble(1, pedido.getValorTotal());
            pst.setString(2, pedido.getStatus().name());
            pst.setDate(3, new Date(pedido.getDtCadastro().getTime()));
            pst.setInt(4, pedido.getTransportadora().getId());
            pst.setInt(5, pedido.getClienteEndereco().getId());
            pst.setInt(6, pedido.getClienteEndereco().getCliente().getId());
            pst.setInt(7, pedido.getClienteEndereco().getEndereco().getId());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir o pedido.");
                }
                int idPedido = rs.getInt(1);
                pedido.setId(idPedido);
            }
            return Resultado.sucesso(pedido);
        }
    }

    // File: PedidoDAO.java

    @Override
    public Resultado<EntidadeDominio> alterar(EntidadeDominio entidade) {
        try {
            Pedido pedido = (Pedido) entidade;

            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            StringBuilder sql = new StringBuilder();
            List<Object> parametros = new ArrayList<>();

            sql.append("UPDATE crud_v3.pedido SET ");

            List<String> campos = new ArrayList<>();

            if (pedido.getStatus() != null) {
                campos.add("ped_status = ?");
                parametros.add(pedido.getStatus().name());
            }
            if (pedido.getValorTotal() != null) {
                campos.add("ped_valor_total = ?");
                parametros.add(pedido.getValorTotal());
            }
            if (pedido.getDtCadastro() != null) {
                campos.add("ped_dt_cadastro = ?");
                parametros.add(new Timestamp(pedido.getDtCadastro().getTime()));
            }

            if (campos.isEmpty()) {
                return Resultado.erro("Nenhum campo para atualizar.");
            }

            sql.append(String.join(", ", campos));
            sql.append(" WHERE ped_id = ?");
            parametros.add(pedido.getId());

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                int linhasAlteradas = pst.executeUpdate();
                if (linhasAlteradas == 0) {
                    return Resultado.erro("Nenhuma linha da tabela pedido foi alterada.");
                }
            }

            connection.commit();
            return Resultado.sucesso(pedido);

        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Erro ao alterar o pedido: " + entidade.getId() + " Erro: " + e.getMessage());
                return Resultado.erro("Erro ao alterar o pedido: " + entidade.getId() + " Erro: " + e.getMessage());
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
        return null;
    }


    @Override
    public Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }

            List<EntidadeDominio> pedidos = new ArrayList<>();
            Pedido pedido = (Pedido) entidade;
            List<Object> parametros = new ArrayList<>();

            String sql = construirConsulta(pedido, parametros);

            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        pedidos.add(mapeiaPedido(rs));
                    }
                }
            }

            return Resultado.sucesso(pedidos);

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Erro ao consultar pedidos: " + e.getMessage());
            return Resultado.erro("Erro ao consultar pedidos: " + e.getMessage());
        }finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar conexão: " + closeEx.getMessage());
            }
        }
    }

    private String construirConsulta(Pedido pedido, List<Object> parametros) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * ");
        sql.append("FROM crud_v3.pedido p ");
        sql.append("INNER JOIN crud_v3.frete f ON p.ped_fre_id = f.fre_id ");
        sql.append("INNER JOIN crud_v3.cliente c ON p.ped_cli_end_cli_id = c.cli_id ");
        sql.append("INNER JOIN crud_v3.cliente_endereco ce ON p.ped_cli_end_id = ce.cli_end_id ");
        sql.append("INNER JOIN crud_v3.endereco e ON p.ped_cli_end_end_id = e.end_id ");
        sql.append("WHERE 1=1 ");

        if (pedido.getId() != null) {
            sql.append(" AND p.ped_id = ? ");
            parametros.add(pedido.getId());
        }
        if (pedido.getValorTotal() != null) {
            sql.append(" AND p.ped_valor_total = ? ");
            parametros.add(pedido.getValorTotal());
        }
        if (pedido.getStatus() != null) {
            sql.append(" AND p.ped_status = ? ");
            parametros.add(pedido.getStatus().name());
        }
        if (pedido.getListStatus() != null && !pedido.getListStatus().isEmpty()) {
            sql.append(" AND p.ped_status IN (");
            sql.append(String.join(",", Collections.nCopies(pedido.getListStatus().size(), "?")));
            sql.append(") ");

            for (Status status : pedido.getListStatus()) {
                parametros.add(status.name());
            }
        }
        if (pedido.getTransportadora() != null) {
            if (pedido.getTransportadora().getId() != null) {
                sql.append(" AND f.fre_id = ? ");
                parametros.add(pedido.getTransportadora().getId());
            }
            if (isStringValida(pedido.getTransportadora().getNome())) {
                sql.append(" AND f.fre_nome = ? ");
                parametros.add(pedido.getTransportadora().getNome());
            }
        }
        if (pedido.getClienteEndereco() != null) {
            ClienteEndereco ce = pedido.getClienteEndereco();
            if (ce.getId() != null) {
                sql.append(" AND ce.cli_end_id = ? ");
                parametros.add(ce.getId());
            }
            if (ce.getCliente() != null) {
                if (ce.getCliente().getId() != null) {
                    sql.append(" AND c.cli_id = ? ");
                    parametros.add(ce.getCliente().getId());
                }
                if (isStringValida(ce.getCliente().getCpf())) {
                    sql.append(" AND c.cli_cpf = ? ");
                    parametros.add(ce.getCliente().getCpf());
                }
            }
            if (ce.getEndereco() != null) {
                if (ce.getEndereco().getId() != null) {
                    sql.append(" AND e.end_id = ? ");
                    parametros.add(ce.getEndereco().getId());
                }
                if (isStringValida(ce.getEndereco().getCep())) {
                    sql.append(" AND e.end_cep = ? ");
                    parametros.add(ce.getEndereco().getCep());
                }
            }
        }

        return sql.toString();
    }

    private boolean isStringValida(String value) {
        return value != null && !value.isBlank();
    }

    private Pedido mapeiaPedido(ResultSet rs) throws SQLException {
        Pedido ped = new Pedido();
        ped.setId(rs.getInt("ped_id"));
        ped.setValorTotal(rs.getDouble("ped_valor_total"));
        ped.setStatus(Status.valueOf(rs.getString("ped_status")));
        ped.setDtCadastro(rs.getTimestamp("ped_dt_cadastro"));

        Transportadora tra = new Transportadora();
        tra.setNome(rs.getString("fre_transportadora"));

        ClienteEndereco ce = new ClienteEndereco();
        Cliente c = new Cliente();

        c.setCpf(rs.getString("cli_cpf"));
        ce.setCliente(c);

        ped.setClienteEndereco(ce);
        ped.setTransportadora(tra);

        return ped;
    }

}
