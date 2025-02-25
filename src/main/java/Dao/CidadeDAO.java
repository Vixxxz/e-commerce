package Dao;

import Dominio.*;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CidadeDAO implements IDAO {
    private Connection connection;

    public CidadeDAO(Connection connection) {
        this.connection = connection;
    }

    public CidadeDAO() {
    }

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        Cidade cidade = (Cidade) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO cidade(cid_nome, cid_uf_id, cid_dt_cadastro) ");
        sql.append("VALUES (?,?,?)");

        if (connection == null) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        IDAO ufDAO = new UfDAO(connection);

        Resultado<List<EntidadeDominio>> resultadoUfs = ufDAO.consultar(cidade.getUf());
        List<EntidadeDominio> ufs = resultadoUfs.getValor();

        if (ufs.isEmpty()) {
            Resultado<EntidadeDominio> resultadoUf = ufDAO.salvar(cidade.getUf());
            cidade.setUf((Uf) resultadoUf.getValor());
        } else {
            cidade.setUf((Uf) ufs.getFirst());
        }

        cidade.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), PreparedStatement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, cidade.getCidade());
            pst.setInt(2, cidade.getUf().getId());
            pst.setTimestamp(3, new Timestamp(cidade.getDtCadastro().getTime()));

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Erro ao salvar Cidade");
                }
                int idCidade = rs.getInt(1);
                cidade.setId(idCidade);
            }
            return Resultado.sucesso(cidade);
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

            Cidade cidade = (Cidade) entidade;
            Resultado<List<EntidadeDominio>> resultadoCidade = consultar(cidade);
            List<EntidadeDominio> cidades = resultadoCidade.getValor();

            if(cidades.isEmpty()) {
                return Resultado.erro("Cidade não cadastrado no sistema");
            }

            UfDAO ufDAO = new UfDAO();
            ufDAO.excluir(cidade.getUf());

            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM crud_v3.cidade c")
                    .append("WHERE c.cid_id = ? ");

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                pst.setInt(1, cidade.getId());
                int rowsDeleted = pst.executeUpdate();

                if (rowsDeleted == 0) {
                    return Resultado.erro("Nenhuma cidade encontrado com o ID fornecido.");
                }
            }

            connection.commit();
            return Resultado.sucesso("Cidade excluida com sucesso");
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

    @Override
    public Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade) {
        try {
            Cidade cidade = (Cidade) entidade;
            List<EntidadeDominio> cidades = new ArrayList<>();
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = construirConsultaCidade(cidade, parametros);

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        cidades.add(mapeiaCidade(rs));
                    }
                }
            }
            return Resultado.sucesso(cidades);
        } catch (Exception e) {
            return Resultado.erro("Erro ao consultar cidade: " + e.getMessage());
        }
    }

    private EntidadeDominio mapeiaCidade(ResultSet rs) throws SQLException {
        Cidade c = new Cidade();
        c.setId(rs.getInt("cid_id"));
        c.setCidade(rs.getString("cid_nome"));

        Uf u = new Uf();
        u.setId(rs.getInt("uf_id"));
        u.setUf(rs.getString("uf_nome"));

        Pais p = new Pais();
        p.setId(rs.getInt("pai_id"));
        p.setPais(rs.getString("pai_nome"));

        u.setPais(p);
        c.setUf(u);

        return c;
    }

    private StringBuilder construirConsultaCidade(Cidade cidade, List<Object> parametros) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM crud_v3.cidade c ")
                .append("INNER JOIN crud_v3.uf u ON c.cid_uf_id = u.uf_id ")
                .append("INNER JOIN crud_v3.pais p ON u.pai_pai_id = p.pai_id ")
                .append("WHERE 1=1 ");

        adicionarCondicao(sql, "c.cid_id = ?", cidade.getId(), parametros);
        adicionarCondicao(sql, "c.cid_nome = ?", cidade.getCidade(), parametros, true);

        if (cidade.getUf() != null) {
            Uf uf = cidade.getUf();
            adicionarCondicao(sql, "u.uf_id = ?", uf.getId(), parametros);
            adicionarCondicao(sql, "u.uf_nome = ?", uf.getUf(), parametros, true);

            if (uf.getPais() != null) {
                Pais pais = uf.getPais();
                adicionarCondicao(sql, "p.pai_id = ?", pais.getId(), parametros);
                adicionarCondicao(sql, "p.pai_nome = ?", pais.getPais(), parametros, true);
            }
        }

        return sql;
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
}
