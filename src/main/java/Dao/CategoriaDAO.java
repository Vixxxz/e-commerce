package Dao;

import Dominio.Categoria;
import Dominio.EntidadeDominio;
import Util.Conexao;
import Util.Resultado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO implements IDAO{
    private Connection connection;

    public CategoriaDAO(Connection connection) {
        this.connection = connection;
    }

    public CategoriaDAO(){}


    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        return null;
    }

    @Override
    public Resultado<EntidadeDominio> alterar(EntidadeDominio entidade) {
        return null;
    }

    @Override
    public Resultado<String> excluir(EntidadeDominio entidade) {
        return null;
    }

    @Override
    public Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade) {
        try{
            if(connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }

            Categoria categoria = (Categoria) entidade;
            List<EntidadeDominio>categorias = new ArrayList<>();
            List<Object>parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * ");
            sql.append("FROM crud_v3.categoria c ");
            sql.append("WHERE 1=1 ");

            if(categoria.getId() != null){
                sql.append("AND c.cat_id=? ");
                parametros.add(categoria.getId());
            }
            if(categoria.getPercentual() != null){
                sql.append("AND c.cat_percentual=? ");
                parametros.add(categoria.getPercentual());
            }
            if(isStringValida(categoria.getNome())){
                sql.append("AND LOWER(c.cat_nome) LIKE? ");
                parametros.add("%" + categoria.getNome().toLowerCase() + "%");
            }

            try(PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        categorias.add(mapeiaCategoria(rs));
                    }
                }
            }
            return Resultado.sucesso(categorias);
        }catch (Exception e) {
            System.err.println("Erro ao consultar as categorias: " + e.getMessage());
            return Resultado.erro("Erro ao consultar as categorias: " + e.getMessage());
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

    private Categoria mapeiaCategoria(ResultSet rs) throws SQLException {
        Categoria cat = new Categoria();
        cat.setId(rs.getInt("cat_id"));
        cat.setNome(rs.getString("cat_nome"));
        cat.setPercentual(rs.getDouble("cat_percentual"));

        return cat;
    }
}
