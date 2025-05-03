package Dao;

import Dominio.*;
import Enums.Status;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TrocaSolicitadaDAO  implements IDAO{
    private Connection connection;

    public TrocaSolicitadaDAO(Connection connection) {
        this.connection = connection;
    }
    public TrocaSolicitadaDAO(){}

    public Resultado<TrocaSolicitada> salvarTroca(TrocaSolicitada trocaSolicitada, List<TrocaSolicitadaTenis> trocaSolicitadaTenis) throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        try{
            Resultado<EntidadeDominio> resultadoSalvarTroca = salvar(trocaSolicitada);
            TrocaSolicitada trocaSalva = (TrocaSolicitada) resultadoSalvarTroca.getValor();
            TrocaSolicitadaTenisDAO trocaSolicitadaTenisDAO = new TrocaSolicitadaTenisDAO(connection);
            for (TrocaSolicitadaTenis trocaProduto : trocaSolicitadaTenis) {
                trocaProduto.setTroca(trocaSalva);
                trocaSolicitadaTenisDAO.salvaTrocaProduto(trocaProduto);
            }

            System.out.println("Troca Solicitada com sucesso!");
            connection.commit();
            return Resultado.sucesso(trocaSalva);
        }catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao salvar troca: " + e.getMessage());
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

        TrocaSolicitada trocaSolicitada = (TrocaSolicitada) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO crud_v3.troca_solicitada(tro_ped_id, tro_status, tro_data_solicitacao, tro_cli_id, tro_valor_total) VALUES (?, ?, ?, ?, ?)");

        trocaSolicitada.complementarDtCadastro();
        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, trocaSolicitada.getPedido().getId());
            pst.setString(2, trocaSolicitada.getStatus().toString());
            pst.setTimestamp(3, new Timestamp(trocaSolicitada.getDtCadastro().getTime()));
            pst.setInt(4, trocaSolicitada.getCliente().getId());
            pst.setDouble(5, trocaSolicitada.getValorTotal());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir o cliente.");
                }
                int trocaId = rs.getInt(1);
                trocaSolicitada.setId(trocaId);
            }

            return Resultado.sucesso(trocaSolicitada);
        }


    }

    @Override
    public Resultado<EntidadeDominio> alterar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        return null;
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

                List<EntidadeDominio> trocasSolicitadas = new ArrayList<>();
                TrocaSolicitada trocaSolicitada = (TrocaSolicitada) entidade;
                List<Object> parametros = new ArrayList<>();

                String sql = construirConsulta(trocaSolicitada, parametros);

                try (PreparedStatement pst = connection.prepareStatement(sql)) {
                    for (int i = 0; i < parametros.size(); i++) {
                        pst.setObject(i + 1, parametros.get(i));
                    }

                    try (ResultSet rs = pst.executeQuery()) {
                        while (rs.next()) {
                            trocasSolicitadas.add(mapeiaTroca(rs));
                        }
                    }
                }

                return Resultado.sucesso(trocasSolicitadas);

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

    private String construirConsulta(TrocaSolicitada trocaSolicitada, List<Object> parametros) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * ");
        sql.append("FROM crud_v3.troca_solicitada t ");
        sql.append("INNER JOIN crud_v3.pedido p ON t.tro_ped_id = p.ped_id ");
        sql.append("INNER JOIN crud_v3.cliente c ON t.tro_cli_id = c.cli_id ");
        sql.append("WHERE 1=1 ");

        if (trocaSolicitada.getId() != null) {
            sql.append(" AND t.tro_id = ? ");
            parametros.add(trocaSolicitada.getId());
        }
        if (trocaSolicitada.getValorTotal() != 0) {
            sql.append(" AND t.tro_valor_total = ? ");
            parametros.add(trocaSolicitada.getValorTotal());
        }
        if (trocaSolicitada.getStatus() != null) {
            sql.append(" AND t.tro_status = ? ");
            parametros.add(trocaSolicitada.getStatus().name());
        }
        if (trocaSolicitada.getListStatus() != null && !trocaSolicitada.getListStatus().isEmpty()) {
            sql.append(" AND p.ped_status IN (");
            sql.append(String.join(",", Collections.nCopies(trocaSolicitada.getListStatus().size(), "?")));
            sql.append(") ");

            for (Status status : trocaSolicitada.getListStatus()) {
                parametros.add(status.name());
            }
        }

        return sql.toString();
    }

    private TrocaSolicitada mapeiaTroca(ResultSet rs) throws SQLException {
        TrocaSolicitada tro = new TrocaSolicitada();
        tro.setId(rs.getInt("tro_id"));
        tro.setValorTotal(rs.getDouble("tro_valor_total"));
        tro.setStatus(Status.valueOf(rs.getString("tro_status")));
        tro.setDtCadastro(rs.getTimestamp("tro_dt_solicitacao"));

        Pedido ped = new Pedido();
        ped.setId(rs.getInt("tro_ped_id"));
        tro.setPedido(ped);

        return tro;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
