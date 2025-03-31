package Dao;

import Dominio.*;
import Util.Conexao;
import Util.Resultado;
import enums.Genero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO implements IDAO{
    private Connection connection;

    public ProdutoDAO(Connection connection) {
        this.connection = connection;
    }

    public ProdutoDAO() {
    }

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        return null;
    }

    @Override
    public Resultado<EntidadeDominio> alterar(EntidadeDominio entidade) {
        try{
            Produto produto = (Produto) entidade;

            MarcaDAO marcaDAO = new MarcaDAO();
            Resultado<List<EntidadeDominio>>resultadoMarca = marcaDAO.consultar(produto.getMarca());
            List<EntidadeDominio>marcas = resultadoMarca.getValor();
            produto.setMarca((Marca) marcas.getFirst());

            CategoriaDAO categoriaDAO = new CategoriaDAO();
            Resultado<List<EntidadeDominio>>resultadoCategoria = categoriaDAO.consultar(produto.getCategoria());
            List<EntidadeDominio>categorias = resultadoCategoria.getValor();
            produto.setCategoria((Categoria) categorias.getFirst());

            if(connection == null || connection.isClosed()){
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE crud_v3.tenis t ");
            sql.append("SET t.ten_sku=?, t.ten_nome =?, t.ten_valor_venda =?, t.ten_modelo=?, t.ten_cor=?, t.ten_tamanho =?, t.ten_genero =?, t.ten_descricao =?, ");
            sql.append("t.ten_cat_id =?, t.ten_mar_id=? ");
            sql.append("WHERE t.ten_id =?");

            try(PreparedStatement pst = connection.prepareStatement(sql.toString())){
                pst.setString(1, produto.getSku());
                pst.setString(2, produto.getNome());
                pst.setDouble(3, produto.getPreco());
                pst.setString(4, produto.getModelo());
                pst.setString(5, produto.getCor());
                pst.setInt(6, produto.getTamanho());
                pst.setObject(7, produto.getGenero());
                pst.setInt(8, produto.getCategoria().getId());
                pst.setInt(9, produto.getMarca().getId());

                int linhasAlteradas = pst.executeUpdate();
                if(linhasAlteradas == 0){
                    return Resultado.erro("Nenhuma linha da tabela tenis foi alterada.");
                }
            }
            connection.commit();
            return Resultado.sucesso(produto);
        }catch (Exception e) {
            System.err.println("Erro ao alterar o produto: "+ entidade.getId() + "Erro: " + e.getMessage());
            return Resultado.erro("Erro ao alterar o produto: "+ entidade.getId() + "Erro: " + e.getMessage());
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
        try{
            if(connection == null || connection.isClosed()){
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);
            Produto produto = (Produto) entidade;
            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM crud_v3.tenis t WHERE t.ten_id =?");
            try(PreparedStatement pst = connection.prepareStatement(sql.toString())){
                pst.setInt(1, produto.getId());
                int linhasAlteradas = pst.executeUpdate();
                if (linhasAlteradas == 0){
                    return Resultado.erro("Registro não encontrado para exclusão.");
                }
            }
            connection.commit();
            return Resultado.sucesso("Produto excluído com sucesso!");
        }catch (Exception e) {
            System.err.println("Erro ao excluir o produto: "+ entidade.getId() + "Erro: " + e.getMessage());
            return Resultado.erro("Erro ao excluir o produto: "+ entidade.getId() + "Erro: " + e.getMessage());
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
            if(connection == null || connection.isClosed()){
                connection = Conexao.getConnectionMySQL();
            }
            List<EntidadeDominio> produtos = new ArrayList<>();
            Produto produto = (Produto) entidade;
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * ");
            sql.append("FROM crud_v3.tenis t ");
            sql.append("INNER JOIN crud_v3.marca m on t.ten_mar_id = m.mar_id ");
            sql.append("INNER JOIN crud_v3.categoria c on t.ten_cat_id = c.cat_id ");
            sql.append("WHERE 1=1 ");

            if(isStringValida(produto.getSku())){
                sql.append(" AND t.ten_sku =? ");
                parametros.add(produto.getSku());
            }
            if(isStringValida(produto.getCor())){
                sql.append(" AND t.ten_cor =? ");
                parametros.add(produto.getCor());
            }
            if(isStringValida(produto.getNome())){
                sql.append(" AND t.ten_nome =? ");
                parametros.add(produto.getNome());
            }
            if(isStringValida(produto.getModelo())){
                sql.append(" AND t.ten_modelo =? ");
                parametros.add(produto.getModelo());
            }
            if(produto.getPreco() != null){
                sql.append(" AND t.ten_valor_venda =? ");
                parametros.add(produto.getPreco());
            }
            if(produto.getGenero() != null){
                sql.append(" AND t.ten_genero =? ");
                parametros.add(produto.getGenero());
            }
            if(produto.getTamanho() != null){
                sql.append(" AND t.ten_tamanho =? ");
                parametros.add(produto.getTamanho());
            }
            if(produto.getMarca() != null){
                if(isStringValida(produto.getMarca().getNome())) {
                    sql.append(" AND m.mar_nome =? ");
                    parametros.add(produto.getMarca().getNome());
                }
            }
            if(produto.getCategoria() != null){
                if(isStringValida(produto.getCategoria().getNome())) {
                    sql.append(" AND c.cat_nome =? ");
                    parametros.add(produto.getCategoria().getNome());
                }
            }

            try(PreparedStatement pst = connection.prepareStatement(sql.toString())){
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        produtos.add(mapeiaProduto(rs));
                    }
                }
            }
            return Resultado.sucesso(produtos);
        }catch (Exception e) {
            System.err.println("Erro ao consultar os produtos: " + e.getMessage());
            return Resultado.erro("Erro ao consultar os produtos: " + e.getMessage());
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

    private Produto mapeiaProduto(ResultSet rs) throws SQLException {
        Produto pro = new Produto();
        pro.setId(rs.getInt("ten_id"));
        pro.setSku(rs.getString("ten_sku"));
        pro.setNome(rs.getString("ten_nome"));
        pro.setPreco(rs.getDouble("ten_valor_venda"));
        pro.setModelo(rs.getString("ten_modelo"));
        pro.setCor(rs.getString("ten_cor"));
        pro.setTamanho(rs.getInt("ten_tamanho"));
        pro.setGenero(rs.getObject("ten_genero", Genero.class));
        pro.setDescricao(rs.getString("ten_desc"));

        Marca mar = new Marca();
        mar.setId(rs.getInt("ten_mar_id"));
        mar.setNome(rs.getString("mar_nome"));

        Categoria cat = new Categoria();
        cat.setId(rs.getInt("ten_cat_id"));
        cat.setNome(rs.getString("cat_nome"));
        cat.setPercentual(rs.getDouble("cat_percentual"));

        pro.setMarca(mar);
        pro.setCategoria(cat);

        return pro;
    }
}
