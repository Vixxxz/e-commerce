package Dao;

import Dominio.EntidadeDominio;
import Dominio.Pais;
import Dominio.Uf;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UfDAO implements IDAO {
    private Connection connection;

    public UfDAO(Connection connection) {
        this.connection = connection;
    }

    public UfDAO() {
    }

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        Uf uf = (Uf) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO uf(uf_nome, uf_pai_id, uf_dt_cadastro) ");
        sql.append(" VALUES (?,?,?)");

        if (connection == null) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        IDAO paisDAO = new PaisDAO(connection);
        Resultado<List<EntidadeDominio>> resultadoPaises = paisDAO.consultar(uf.getPais());
        List<EntidadeDominio> paises = resultadoPaises.getValor();

        if (paises.isEmpty()) {
            Resultado<EntidadeDominio> resultadoPais = paisDAO.salvar(uf.getPais());
            uf.setPais((Pais) resultadoPais.getValor());
        } else {
            uf.setPais((Pais) paises.getFirst());
        }

        uf.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, uf.getUf());
            pst.setInt(2, uf.getPais().getId());
            pst.setTimestamp(3, new Timestamp(uf.getDtCadastro().getTime()));

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Erro ao salvar UF");
                }
                int idUf = rs.getInt(1);
                uf.setId(idUf);
            }
            return Resultado.sucesso(uf);
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

            Uf uf = (Uf) entidade;
            Resultado<List<EntidadeDominio>> resultadoUf = consultar(uf);
            List<EntidadeDominio> ufs = resultadoUf.getValor();

            if(ufs.isEmpty()) {
                return Resultado.erro("Uf não cadastrado no sistema");
            }

            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM crud_v3.uf u ")
                    .append("WHERE u.uf_id = ? ");

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                pst.setInt(1, uf.getId());
                int rowsDeleted = pst.executeUpdate();
                System.out.println("linhas deletadas: " + rowsDeleted);
                if (rowsDeleted == 0) {
                    return Resultado.erro("Nenhuma uf encontrado com o ID fornecido.");
                }
            }

            return Resultado.sucesso("Uf excluida com sucesso");
        }catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao deletar uf: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Erro durante rollback: " + rollbackEx.getMessage());
                return Resultado.erro("Erro durante rollback: " + rollbackEx.getMessage());
            }
        }
    }

    @Override
    public Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade) {
        try{
            Uf uf = (Uf) entidade;
            List<EntidadeDominio> ufs = new ArrayList<>();
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder();
            sql.append("select * from crud_v3.uf u ");
            sql.append("inner join crud_v3.pais p on u.uf_pai_id = p.pai_id ");
            sql.append("where 1=1 ");

            if(uf.getId() != null){
                sql.append(" and u.uf_id = ? ");
                parametros.add(uf.getId());
            }
            if(isStringValida(uf.getUf())){
                sql.append(" and u.uf_nome = ? ");
                parametros.add(uf.getUf());
            }
            if(uf.getPais() != null){
                Pais p = uf.getPais();
                if(p.getId() != null){
                    sql.append(" and p.pai_id = ? ");
                    parametros.add(p.getId());
                }
                if(isStringValida(p.getPais())){
                    sql.append(" and p.pai_nome = ? ");
                    parametros.add(p.getPais());
                }
            }
            try(PreparedStatement pst = connection.prepareStatement(sql.toString())){
                for(int i = 0; i < parametros.size(); i++){
                    pst.setObject(i + 1, parametros.get(i));
                }
                try(ResultSet rs = pst.executeQuery()){
                    while (rs.next()){
                        Uf u = new Uf();
                        u.setId(rs.getInt("uf_id"));
                        u.setUf(rs.getString("uf_nome"));

                        Pais p = new Pais();
                        p.setId(rs.getInt("pai_id"));
                        p.setPais(rs.getString("pai_nome"));

                        u.setPais(p);

                        ufs.add(u);
                    }
                }
            }
            return Resultado.sucesso(ufs);
        } catch (Exception e) {
            return Resultado.erro("Erro ao buscar uf: " + e.getMessage());
        }
    }

    private boolean isStringValida(String value) {
        return value != null && !value.isBlank();
    }
}
