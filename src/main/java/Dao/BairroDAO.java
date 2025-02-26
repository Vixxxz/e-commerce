package Dao;

import Dominio.*;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BairroDAO implements IDAO {
    private Connection connection;
    public BairroDAO(Connection connection) {
        this.connection = connection;
    }
    public BairroDAO() {}

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        if (connection == null) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        Bairro bairro = (Bairro) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO bairro(bai_nome, bai_cid_id, bai_dt_cadastro) ");
        sql.append("VALUES (?,?,?)");

        if (connection == null) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        IDAO cidadeDAO = new CidadeDAO(connection);

        Resultado<List<EntidadeDominio>> resultadoCidades = cidadeDAO.consultar(bairro.getCidade());
        List<EntidadeDominio> cidades = resultadoCidades.getValor();

        if (cidades.isEmpty()) {
            Resultado<EntidadeDominio> resultadoCidade = cidadeDAO.salvar(bairro.getCidade());
            bairro.setCidade((Cidade) resultadoCidade.getValor());
        } else {
            bairro.setCidade((Cidade) cidades.getFirst());
        }

        bairro.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, bairro.getBairro());
            pst.setInt(2, bairro.getCidade().getId());
            pst.setTimestamp(3, new Timestamp(bairro.getDtCadastro().getTime()));

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Erro ao criar bairro");
                }
                int idBairro = rs.getInt(1);
                bairro.setId(idBairro);
            }
            return Resultado.sucesso(bairro);
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

            Bairro bairro = (Bairro) entidade;
            Resultado<List<EntidadeDominio>> resultadoBairro = consultar(bairro);
            List<EntidadeDominio> bairros = resultadoBairro.getValor();

            if(bairros.isEmpty()) {
                return Resultado.erro("Bairro não cadastrado no sistema");
            }

            CidadeDAO cidadeDAO = new CidadeDAO();
            cidadeDAO.excluir(bairro.getCidade());

            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM crud_v3.bairro b")
                    .append("WHERE b.bai_id = ? ");

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                pst.setInt(1, bairro.getId());
                int rowsDeleted = pst.executeUpdate();

                if (rowsDeleted == 0) {
                    return Resultado.erro("Nenhum Bairro encontrado com o ID fornecido.");
                }
            }
            connection.commit();
            return Resultado.sucesso("Bairro excluido com sucesso");
        }catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao deletar bairro: " + e.getMessage());
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
        Bairro bairro = (Bairro) entidade;
        try {
            List<EntidadeDominio> bairros = new ArrayList<>();
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = construirConsultaBairro(bairro, parametros);

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        bairros.add(mapeiaBairro(rs));
                    }
                }
            }
            return Resultado.sucesso(bairros);
        } catch (Exception e) {
            return Resultado.erro("Erro ao consultar bairro: " + e.getMessage());
        }
    }

    private EntidadeDominio mapeiaBairro(ResultSet rs) throws SQLException {
        Bairro bairro = new Bairro();
        bairro.setId(rs.getInt("bai_id"));
        bairro.setBairro(rs.getString("bai_nome"));

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
        bairro.setCidade(cidade);

        return bairro;
    }

    private StringBuilder construirConsultaBairro(Bairro bairro, List<Object> parametros) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM crud_v3.bairro b ")
                .append("INNER JOIN crud_v3.cidade c ON b.bai_cid_id = c.cid_id ")
                .append("INNER JOIN crud_v3.uf u ON c.cid_uf_id = u.uf_id ")
                .append("INNER JOIN crud_v3.pais p ON u.uf_pai_id = p.pai_id ")
                .append("WHERE 1=1 ");

        adicionarCondicao(sql, "b.bai_id = ?", bairro.getId(), parametros);
        adicionarCondicao(sql, "b.bai_nome = ?", bairro.getBairro(), parametros, true);

        if (bairro.getCidade() != null) {
            Cidade cidade = bairro.getCidade();
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
