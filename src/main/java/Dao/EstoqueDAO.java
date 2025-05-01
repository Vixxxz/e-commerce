package Dao;

import Dominio.*;
import Enums.Genero;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstoqueDAO implements IDAO{
    private Connection connection;

    public EstoqueDAO(Connection connection) {
        this.connection = connection;
    }

    public EstoqueDAO() {
    }

    public Resultado<EntidadeDominio>atualizarEstoque(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        Connection connectionAntiga = connection;
        connection = Conexao.getConnectionMySQL();
        switch (entidade){
            case PedidoProduto pedidoProduto ->{
                Estoque est = new Estoque();
                est.setProduto(pedidoProduto.getProduto());
                Resultado<List<EntidadeDominio>>resultadoEstoque = consultar(est);
                List<EntidadeDominio>estoques = resultadoEstoque.getValor();
                if(estoques.isEmpty()){
                    return Resultado.erro("Não existe estoque para o produto selecionado: " + pedidoProduto.getProduto().getSku());
                }
                connection = connectionAntiga;
                Estoque ultimoEstoque = (Estoque) estoques.getFirst();
                est.setQuantidade(ultimoEstoque.getQuantidade() - pedidoProduto.getQuantidade());
                est.setMovimentacao(-pedidoProduto.getQuantidade());
                est.setValorCusto(ultimoEstoque.getValorCusto());
                est.setMarca(ultimoEstoque.getMarca());
                Resultado<EntidadeDominio>resultadoSalvaEstoque = salvar(est);
                if(!resultadoSalvaEstoque.isSucesso()){
                    return Resultado.erro("Não foi possível atualizar o estoque.");
                }
                return Resultado.sucesso(est);
            }
            case DevolucaoProduto devolucaoProduto ->{
                Estoque est = new Estoque();
                est.setProduto(devolucaoProduto.getProduto());
                Resultado<List<EntidadeDominio>>resultadoEstoque = consultar(est);
                List<EntidadeDominio>estoques = resultadoEstoque.getValor();
                if(estoques.isEmpty()){
                    return Resultado.erro("Não existe estoque para o produto selecionado: " + devolucaoProduto.getProduto().getSku());
                }
                connection = connectionAntiga;
                Estoque ultimoEstoque = (Estoque) estoques.getFirst();
                est.setQuantidade(ultimoEstoque.getQuantidade() + devolucaoProduto.getQuantidade());
                est.setMovimentacao(devolucaoProduto.getQuantidade());
                est.setValorCusto(ultimoEstoque.getValorCusto());
                est.setMarca(ultimoEstoque.getMarca());
                Resultado<EntidadeDominio>resultadoSalvaEstoque = salvar(est);
                if(!resultadoSalvaEstoque.isSucesso()){
                    return Resultado.erro("Não foi possível atualizar o estoque.");
                }
                return Resultado.sucesso(est);
            }
            default -> {
                return Resultado.erro("Nenhuma entidade valida");
            }
        }
    }

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        Estoque estoque = (Estoque) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO estoque(est_quantidade, est_dt_cadastro, est_ten_id, est_valor_custo, ");
        sql.append("est_mar_id, est_movimentacao) ");
        sql.append("VALUES (?,?,?,?,?,?)");

        estoque.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, estoque.getQuantidade());
            pst.setDate(2, new Date(estoque.getDtCadastro().getTime()));
            pst.setInt(3, estoque.getProduto().getId());
            pst.setDouble(4, estoque.getValorCusto());
            pst.setInt(5, estoque.getMarca().getId());
            pst.setInt(6, estoque.getMovimentacao());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir o estoque.");
                }
                int idEstoque = rs.getInt(1);
                estoque.setId(idEstoque);
            }
            return Resultado.sucesso(estoque);
        }
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
            sql.append(" ORDER BY e.est_dt_cadastro DESC ");

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

    public Resultado<String> reserva(EntidadeDominio entidade) {
        try{
            if(connection == null || connection.isClosed()){
                connection = Conexao.getConnectionMySQL();
            }

            Estoque estoque = (Estoque) entidade;

            StringBuilder sql = new StringBuilder("SELECT e.est_quantidade FROM estoque e WHERE e.est_ten_id = ? AND e.est_mar_id = ? FOR UPDATE");

            try(PreparedStatement pst = connection.prepareStatement(sql.toString())){
                pst.setInt(1, estoque.getProduto().getId());
                pst.setInt(2, estoque.getMarca().getId());

                pst.executeQuery();
            }
            return Resultado.sucesso("Linhas bloqueadas com sucesso");
        }catch (Exception e) {
            System.err.println("Erro ao bloquear linhas para reserva: " + e.getMessage());
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
            return Resultado.erro("Erro ao bloquear linhas para reserva: " + e.getMessage());
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

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
