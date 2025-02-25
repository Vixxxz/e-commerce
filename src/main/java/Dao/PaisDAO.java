package Dao;

import Dominio.EntidadeDominio;
import Dominio.Pais;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaisDAO implements IDAO {
    private Connection connection;

    public PaisDAO(Connection connection) {
        this.connection = connection;
    }

    public PaisDAO() {
    }

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        if (connection == null) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        Pais pais = (Pais) entidade;
        StringBuilder sql = new StringBuilder();

        sql.append("INSERT INTO pais(pai_nome, pai_dt_cadastro) VALUES (?, ?)");

        pais.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, pais.getPais());
            pst.setTimestamp(2, new Timestamp(pais.getDtCadastro().getTime()));
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    int idPais = rs.getInt(1);
                    pais.setId(idPais);
                }
            }
            return pais;
        }
    }

    @Override
    public Resultado<EntidadeDominio> alterar(EntidadeDominio entidade) {

        return null;
    }

    @Override
    public Resultado<String> excluir(EntidadeDominio entidade) {
        try{
            if (connection == null) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            Pais pais = (Pais) entidade;
            Resultado<List<EntidadeDominio>> resultadoPais = consultar(pais);
            List<EntidadeDominio> paises = resultadoPais.getValor();

            if(paises.isEmpty()) {
                return Resultado.erro("Pais não cadastrado no sistema");
            }

            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM crud_v3.pais p")
                    .append("WHERE p.pai_id = ? ");

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                pst.setInt(1, pais.getId());
                int rowsDeleted = pst.executeUpdate();

                if (rowsDeleted == 0) {
                    return Resultado.erro("Nenhum pais encontrado com o ID fornecido.");
                }
            }

            connection.commit();
            return Resultado.sucesso("Pais excluido com sucesso");
        }catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao deletar pais: " + e.getMessage());
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
    public Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade) {
        try{
            Pais pais = (Pais) entidade;
            List<EntidadeDominio> paises = new ArrayList<>();
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder();
            sql.append("select * from crud_v3.pais p ");
            sql.append("where 1=1 ");

            if (pais.getId() != null) {
                sql.append(" and p.pai_id = ? ");
                parametros.add(pais.getId());
            }
            if (isStringValida(pais.getPais())) {
                sql.append(" and p.pai_nome = ? ");
                parametros.add(pais.getPais());
            }

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Pais p = new Pais();
                        p.setId(rs.getInt("pai_id"));
                        p.setPais(rs.getString("pai_nome"));
                        paises.add(p);
                    }
                }
            }
            return Resultado.sucesso(paises);
        }catch (Exception e) {
            return Resultado.erro("Erro ao consultar pais: " + e.getMessage());
        }
    }

    private boolean isStringValida(String value) {
        return value != null && !value.isBlank();
    }
}
