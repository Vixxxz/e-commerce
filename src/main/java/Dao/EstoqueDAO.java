package Dao;

import Dominio.*;
import Enums.Genero;
import Util.Conexao;
import Util.Resultado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstoqueDAO implements IDAO{
    private Connection connection;

    public EstoqueDAO(Connection connection) {
        this.connection = connection;
    }

    public EstoqueDAO() {
    }

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
            if(connection == null || connection.isClosed()){
                connection = Conexao.getConnectionMySQL();
            }

            Estoque estoque = (Estoque) entidade;
            List<EntidadeDominio> estoques = new ArrayList<>();
            List<Object>parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder("SELECT * FROM crud_v3.estoque e ");
            sql.append("INNER JOIN crud_v3.tenis t on t.ten_id = e.est_ten_id ");
            sql.append("WHERE 1 = 1 ");

            if(estoque.getId() != null){
                sql.append("AND e.est_id =? ");
                parametros.add(estoque.getId());
            }
            if(estoque.getQuantidade() != null){
                sql.append("AND e.est_quantidade =? ");
                parametros.add(estoque.getQuantidade());
            }
            if(estoque.getMovimentacao() != null){
                sql.append("AND e.est_movimentacao =? ");
                parametros.add(estoque.getMovimentacao());
            }
            if(estoque.getValorCusto() != null){
                sql.append("AND e.est_valor_custo =? ");
                parametros.add(estoque.getValorCusto());
            }
            if(estoque.getProduto() != null){
                Produto produto = estoque.getProduto();
                if(produto.getId() != null){
                    sql.append("AND t.ten_id =? ");
                    parametros.add(produto.getId());
                }
                if(produto.getSku() != null && !produto.getSku().isBlank()){
                    sql.append("AND t.ten_sku =? ");
                    parametros.add(produto.getSku());
                }
            }

            try(PreparedStatement pst = connection.prepareStatement(sql.toString())){
                for(int i = 0; i < parametros.size(); i++){
                    pst.setObject(i+1, parametros.get(i));
                }

                try(ResultSet rs = pst.executeQuery()){
                    while(rs.next()){
                        estoques.add(mapeiaEstoque(rs));
                    }
                }
            }
            return Resultado.sucesso(estoques);
        }catch (Exception e) {
            System.err.println("Erro ao consultar o estoque: " + e.getMessage());
            return Resultado.erro("Erro ao consultar o estoque: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }

    private Estoque mapeiaEstoque(ResultSet rs) throws SQLException {
        Produto pro = new Produto();
        pro.setId(rs.getInt("ten_id"));
        pro.setSku(rs.getString("ten_sku"));
        pro.setNome(rs.getString("ten_nome"));
        pro.setPreco(rs.getDouble("ten_valor_venda"));
        pro.setModelo(rs.getString("ten_modelo"));
        pro.setCor(rs.getString("ten_cor"));
        pro.setTamanho(rs.getInt("ten_tamanho"));
        pro.setGenero(Genero.valueOf(rs.getString("ten_genero")));
        pro.setDescricao(rs.getString("ten_desc"));

        Estoque est = new Estoque();
        est.setId(rs.getInt("est_id"));
        est.setQuantidade(rs.getInt("est_quantidade"));
        est.setValorCusto(rs.getDouble("est_valor_custo"));
        est.setMovimentacao(rs.getInt("est_movimentacao"));
        est.setDtCadastro(rs.getTimestamp("est_dt_cadastro"));

        Marca mar = new Marca();
        mar.setId(rs.getInt("est_mar_id"));

        est.setMarca(mar);
        est.setProduto(pro);

        return est;
    }
}
