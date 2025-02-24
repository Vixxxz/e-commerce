package Dao;

import Dominio.*;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnderecoDAO implements IDAO {
    private Connection connection;

    public EnderecoDAO(Connection connection) {
        this.connection = connection;
    }

    public EnderecoDAO() {
    }


    @Override
    public EntidadeDominio salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        Endereco endereco = (Endereco) entidade;
        StringBuilder sql = new StringBuilder();

        sql.append("INSERT INTO endereco(end_cep, end_bai_id, end_logradouro, end_tp_logradouro, end_dt_cadastro) ");
        sql.append("VALUES(?,?,?,?,?)");

        if (connection == null) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        IDAO bairroDAO = new BairroDAO(connection);

        Resultado<List<EntidadeDominio>> resultadoBairro = bairroDAO.consultar(endereco.getBairro());
        List<EntidadeDominio> bairros = resultadoBairro.getValor();

        if (bairros.isEmpty()) {
            endereco.setBairro((Bairro) bairroDAO.salvar(endereco.getBairro()));
        } else {
            endereco.setBairro((Bairro) bairros.getFirst());
        }

        endereco.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, endereco.getCep());
            pst.setInt(2, endereco.getBairro().getId());
            pst.setString(3, endereco.getLogradouro());
            pst.setString(4, endereco.getTipoLogradouro());
            pst.setTimestamp(5, new Timestamp(endereco.getDtCadastro().getTime()));

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    int idEndereco = rs.getInt(1);
                    endereco.setId(idEndereco);
                }
            }
            return endereco;
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

            Endereco endereco = (Endereco) entidade;
            Resultado<List<EntidadeDominio>> resultadoEndereco = consultar(endereco);
            List<EntidadeDominio> enderecos = resultadoEndereco.getValor();

            if(enderecos.isEmpty()) {
                return Resultado.erro("Endereco não cadastrado no sistema");
            }

            BairroDAO bairroDAO = new BairroDAO();
            bairroDAO.excluir(endereco.getBairro());

            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM crud_v3.endereco e")
                    .append("WHERE e.end_id = ? ");

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                pst.setInt(1, endereco.getId());
                int rowsDeleted = pst.executeUpdate();

                if (rowsDeleted == 0) {
                    return Resultado.erro("Nenhum endereco encontrado com o ID fornecido.");
                }
            }
            connection.commit();
            return Resultado.sucesso("endereco excluido com sucesso");
        }catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao deletar endereco: " + e.getMessage());
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
            Endereco endereco = (Endereco) entidade;
            List<EntidadeDominio> enderecos = new ArrayList<>();
            List<Object> parametros = new ArrayList<>();
            StringBuilder sql = construirConsultaEndereco(endereco, parametros);

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        enderecos.add(mapeiaEndereco(rs));
                    }
                }
            }
            return Resultado.sucesso(enderecos);
        } catch (Exception e) {
            return Resultado.erro("Erro ao consultar ClienteEndereco");
        }
    }

    private StringBuilder construirConsultaEndereco(Endereco endereco, List<Object> parametros) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM crud_v3.endereco e ")
                .append("INNER JOIN crud_v3.bairro b            ON e.end_bai_id = b.bai_id ")
                .append("INNER JOIN crud_v3.cidade c            ON b.bai_cid_id = c.cid_id ")
                .append("INNER JOIN crud_v3.uf u                ON c.cid_uf_id = u.uf_id ")
                .append("INNER JOIN crud_v3.pais p              ON u.pai_pai_id = p.pai_id ")
                .append("WHERE 1=1 ");

        adicionarCondicao(sql, "e.end_id = ?", endereco.getId(), parametros);
        adicionarCondicao(sql, "e.end_cep = ?", endereco.getCep(), parametros, true);
        adicionarCondicao(sql, "e.end_logradouro = ?", endereco.getLogradouro(), parametros, true);
        adicionarCondicao(sql, "e.end_tp_logradouro = ?", endereco.getTipoLogradouro(), parametros, true);

        if (endereco.getBairro() != null) {
            Bairro bairro = endereco.getBairro();
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

    private Endereco mapeiaEndereco(ResultSet rs) throws SQLException {
        Endereco e = new Endereco();
        e.setId(rs.getInt("end_id"));
        e.setCep(rs.getString("end_cep"));
        e.setLogradouro(rs.getString("end_logradouro"));
        e.setTipoLogradouro(rs.getString("end_tp_logradouro"));

        Bairro b = new Bairro();
        b.setId(rs.getInt("bai_id"));
        b.setBairro(rs.getString("bai_nome"));

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
        b.setCidade(c);
        e.setBairro(b);

        return e;
    }
}
