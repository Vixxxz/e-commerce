package Dao;

import Dominio.*;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
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
                TrocaSolicitadaTenisDAO.salvaTrocaProduto(trocaProduto);
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
        sql.append("INSERT INTO crud_v3.troca_solicitada(tro_ped, tro_status, tro_dt_cadastro) VALUES (?, ?, ?)");

        trocaSolicitada.complementarDtCadastro();
        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, trocaSolicitada.getPedido().getId());
            pst.setString(2, trocaSolicitada.getStatus().toString());
            pst.setTimestamp(3, new Timestamp(trocaSolicitada.getDtCadastro().getTime()));
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

            List<EntidadeDominio> trocas = new ArrayList<>();
            TrocaSolicitada trocaSolicitada = (TrocaSolicitada) entidade;
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM crud_v3.troca_solicitada ts ");
            sql.append("INNER JOIN crud_v3.cliente c on ts.tro_cli_id = c.cli_id ");
            sql.append("WHERE 1=1 ");

            if (trocaSolicitada.getId() != null) {
                sql.append("AND ts.tro_id = ? ");
                parametros.add(trocaSolicitada.getId());
            }
            if(trocaSolicitada.getPedido() != null){
                if(trocaSolicitada.getPedido().getId() != null){
                    sql.append("AND ts.tro_ped_id = ? ");
                    parametros.add(trocaSolicitada.getPedido().getId());
                }
            }
            if (trocaSolicitada.getStatus() != null) {
                sql.append("AND ts.tro_status = ? ");
                parametros.add(trocaSolicitada.getStatus());
            }
            if (trocaSolicitada.getCliente() != null) {
                if(trocaSolicitada.getCliente().getId() != null){
                    sql.append("AND ts.tro_cli_id = ? ");
                    parametros.add(trocaSolicitada.getCliente().getId());
                }
            }

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        trocas.add(mapeiaTroca(rs));
                    }
                }
            }
            return Resultado.sucesso(trocas);
        } catch (Exception e) {
            System.err.println("Erro ao consultar as transportadoras: " + e.getMessage());
            return Resultado.erro("Erro ao consultar as transportadoras: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }

    //todo: mapear troca
    private TrocaSolicitada mapeiaTroca(ResultSet rs) throws SQLException {
//        Transportadora tra = new Transportadora();
//        tra.setId(rs.getInt("fre_id"));
//        tra.setNome(rs.getString("fre_transportadora"));
//        tra.setValor(Double.valueOf(rs.getString("fre_valor")));
//
//        return tra;
        return null;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
