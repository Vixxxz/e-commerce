package Dao;

import Dominio.*;
import Enums.Ativo;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO implements IDAO {
    private Connection connection;

    public ReservaDAO(Connection connection) {
        this.connection = connection;
    }

    public ReservaDAO() {
    }

    public Resultado<String> CriaOuAtualizaReserva(EntidadeDominio entidade) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);
            ReservaEstoque reservaEstoque = (ReservaEstoque) entidade;

            ReservaEstoque filtro = new ReservaEstoque();
            filtro.setSessao(reservaEstoque.getSessao());
            filtro.setProduto(reservaEstoque.getProduto());

            Resultado<List<EntidadeDominio>> resultadoFiltro = consultar(filtro);
            List<EntidadeDominio> filtros = resultadoFiltro.getValor();

            if (filtros.isEmpty()) {
                Resultado<EntidadeDominio> resultadoSalvaReserva = salvar(reservaEstoque);
                if (!resultadoSalvaReserva.isSucesso()) {
                    return Resultado.erro(resultadoSalvaReserva.getErro());
                }
            } else {
                Resultado<EntidadeDominio> resultadoAtualizaReserva = alterar(reservaEstoque);
                if (!resultadoAtualizaReserva.isSucesso()) {
                    return Resultado.erro(resultadoAtualizaReserva.getErro());
                }
            }
            connection.commit();
            return Resultado.sucesso("Reserva feita com sucesso");
        } catch (Exception e) {
            System.err.println("Erro ao salvar a reserva: " + e.getMessage());
            return Resultado.erro("Erro ao salvar a reserva: " + e.getMessage());
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
        ReservaEstoque reservaEstoque = (ReservaEstoque) entidade;

        EstoqueDAO estoqueDAO = new EstoqueDAO(connection);
        Estoque estoque = new Estoque();

        estoque.setProduto(reservaEstoque.getProduto());
        estoque.setMarca(reservaEstoque.getMarca());

        estoqueDAO.reserva(estoque);

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO estoque_reserva (res_ten_id, res_mar_id, res_qtd, res_data, res_sessao) ");
        sql.append("VALUES (?, ?, ?, ?, ?)");

        reservaEstoque.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, reservaEstoque.getProduto().getId());
            pst.setInt(2, reservaEstoque.getMarca().getId());
            pst.setInt(3, reservaEstoque.getQuantidade());
            pst.setTimestamp(4, new Timestamp(reservaEstoque.getDtCadastro().getTime()));
            pst.setString(5, reservaEstoque.getSessao());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir a reserva.");
                }
                int idReserva = rs.getInt(1);
                reservaEstoque.setId(idReserva);
            }
        }
        return Resultado.sucesso(reservaEstoque);
    }

    @Override
    public Resultado<EntidadeDominio> alterar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            connection = Conexao.getConnectionMySQL();
        }

        connection.setAutoCommit(false);
        ReservaEstoque reservaEstoque = (ReservaEstoque) entidade;

        EstoqueDAO estoqueDAO = new EstoqueDAO(connection);
        Estoque estoque = new Estoque();
        estoque.setProduto(reservaEstoque.getProduto());
        estoque.setMarca(reservaEstoque.getMarca());
        estoqueDAO.reserva(estoque);

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE estoque_reserva ");
        sql.append("SET res_qtd = ?, res_data = ? ");
        sql.append("WHERE res_ten_id = ? AND res_mar_id = ? AND res_sessao = ?");

        reservaEstoque.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
            pst.setInt(1, reservaEstoque.getQuantidade());
            pst.setTimestamp(2, new Timestamp(reservaEstoque.getDtCadastro().getTime()));
            pst.setInt(3, reservaEstoque.getProduto().getId());
            pst.setInt(4, reservaEstoque.getMarca().getId());
            pst.setString(5, reservaEstoque.getSessao());

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Nenhum registro foi atualizado. Verifique os critérios da atualização.");
            }
        }

        return Resultado.sucesso(reservaEstoque);
    }

    public Resultado<EntidadeDominio> concluiReserva(EntidadeDominio entidade) {
        try{
            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }

            connection.setAutoCommit(false);
            ReservaEstoque reservaEstoque = (ReservaEstoque) entidade;
            reservaEstoque.setStatus(Ativo.CONCLUIDO);

            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE estoque_reserva ");
            sql.append("SET res_status = ? ");
            sql.append("WHERE res_ten_id = ? AND res_mar_id = ? AND res_sessao = ?");

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                pst.setString(1, reservaEstoque.getStatus().name());
                pst.setInt(2, reservaEstoque.getProduto().getId());
                pst.setInt(3, reservaEstoque.getMarca().getId());
                pst.setString(4, reservaEstoque.getSessao());

                int rowsAffected = pst.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("Nenhum registro foi atualizado. Verifique os critérios da atualização.");
                }

                connection.commit();
            }
            return Resultado.sucesso(reservaEstoque);
        }catch (Exception e) {
            System.err.println("Erro ao concluir as reservas: " + e.getMessage());
            return Resultado.erro("Erro ao concluir as reservas: " + e.getMessage());
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

            ReservaEstoque reservaEstoque = (ReservaEstoque) entidade;
            List<EntidadeDominio> reservas = new ArrayList<>();
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder("SELECT * FROM crud_v3.estoque_reserva er ");
            sql.append("WHERE 1 = 1 ");

            if (reservaEstoque.getId() != null) {
                sql.append("AND er.res_id =? ");
                parametros.add(reservaEstoque.getId());
            }
            if (reservaEstoque.getProduto() != null) {
                if (reservaEstoque.getProduto().getId() != null) {
                    sql.append("AND er.res_ten_id =? ");
                    parametros.add(reservaEstoque.getProduto().getId());
                }
            }
            if (reservaEstoque.getMarca() != null) {
                if (reservaEstoque.getMarca().getId() != null) {
                    sql.append("AND er.res_mar_id =? ");
                    parametros.add(reservaEstoque.getMarca().getId());
                }
            }
            if (reservaEstoque.getQuantidade() != null) {
                sql.append("AND er.res_qtd =? ");
                parametros.add(reservaEstoque.getQuantidade());
            }
            if (reservaEstoque.getDtCadastro() != null) {
                sql.append("AND er.res_data =? ");
                parametros.add(reservaEstoque.getDtCadastro());
            }
            if (reservaEstoque.getSessao() != null) {
                sql.append("AND er.res_sessao =? ");
                parametros.add(reservaEstoque.getSessao());
            }
            if(reservaEstoque.getStatus() != null){
                sql.append("AND er.res_status =?");
                parametros.add(reservaEstoque.getStatus().name());
            }

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        reservas.add(mapeiaReserva(rs));
                    }
                }
            }
            return Resultado.sucesso(reservas);
        } catch (Exception e) {
            System.err.println("Erro ao consultar as reservas: " + e.getMessage());
            return Resultado.erro("Erro ao consultar as reservas: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }

    private ReservaEstoque mapeiaReserva(ResultSet rs) throws SQLException {
        Produto pro = new Produto();
        pro.setId(rs.getInt("res_ten_id"));

        ReservaEstoque res = new ReservaEstoque();
        res.setId(rs.getInt("res_id"));
        res.setQuantidade(rs.getInt("res_qtd"));
        res.setDtCadastro(rs.getTimestamp("res_data"));
        res.setSessao(rs.getString("res_sessao"));

        Marca mar = new Marca();
        mar.setId(rs.getInt("res_mar_id"));

        res.setMarca(mar);
        res.setProduto(pro);

        return res;
    }
}
