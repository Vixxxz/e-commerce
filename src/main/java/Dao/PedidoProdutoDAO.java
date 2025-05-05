package Dao;

import Dominio.*;
import Enums.Genero;
import Enums.Status;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PedidoProdutoDAO implements IDAO{
    private Connection connection;

    public PedidoProdutoDAO(Connection connection) {
        this.connection = connection;
    }

    public PedidoProdutoDAO() {
    }

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        PedidoProduto pedidoProduto = (PedidoProduto) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO pedido_tenis(ped_ten_ped_id, ped_ten_ten_id, ped_ten_quantidade, ped_ten_dt_adicao) ");
        sql.append("VALUES (?,?,?,?)");

        pedidoProduto.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setDouble(1, pedidoProduto.getPedido().getId());
            pst.setInt(2, pedidoProduto.getProduto().getId());
            pst.setInt(3, pedidoProduto.getQuantidade());
            pst.setDate(4, new Date(pedidoProduto.getDtCadastro().getTime()));
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir o pedido.");
                }
                int idPedidoProduto = rs.getInt(1);
                pedidoProduto.setId(idPedidoProduto);
            }
            return Resultado.sucesso(pedidoProduto);
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
        try {
            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }

            List<EntidadeDominio> pedidoProdutos = new ArrayList<>();
            PedidoProduto pedidoProduto = (PedidoProduto) entidade;
            List<Object> parametros = new ArrayList<>();

            String sql = construirConsulta(pedidoProduto, parametros);

            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        pedidoProdutos.add(mapeiaPedidoProduto(rs));
                    }
                }
            }

            return Resultado.sucesso(pedidoProdutos);

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Erro ao consultar os produtos do pedido: " + e.getMessage());
            return Resultado.erro("Erro ao consultar os produtos do pedido: " + e.getMessage());
        }finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar conexão: " + closeEx.getMessage());
            }
        }
    }

    private String construirConsulta(PedidoProduto pedidoProduto, List<Object> parametros) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * ");
        sql.append("FROM crud_v3.pedido_tenis pt ");
        sql.append("INNER JOIN crud_v3.pedido p ON pt.ped_ten_ped_id = p.ped_id ");
        sql.append("INNER JOIN crud_v3.tenis t ON pt.ped_ten_ten_id = t.ten_id ");
        sql.append("WHERE 1=1 ");

        if(pedidoProduto.getId() != null){
            sql.append(" AND pt.ped_ten_id = ? ");
            parametros.add(pedidoProduto.getId());
        }

        if(pedidoProduto.getQuantidade() != null){
            sql.append(" AND pt.ped_ten_quantidade = ? ");
            parametros.add(pedidoProduto.getQuantidade());
        }

        if (pedidoProduto.getPedido() != null) {
            Pedido pedido = pedidoProduto.getPedido();
            if(pedido.getId() != null){
                sql.append(" AND p.ped_id = ? ");
                parametros.add(pedido.getId());
            }
            if (pedido.getValorTotal() != null) {
                sql.append(" AND p.ped_valor_total = ? ");
                parametros.add(pedido.getValorTotal());
            }
            if (pedido.getStatus() != null) {
                sql.append(" AND p.ped_status = ? ");
                parametros.add(pedido.getStatus().name());
            }
            if (pedido.getListStatus() != null && !pedido.getListStatus().isEmpty()) {
                sql.append(" AND p.ped_status IN (");
                sql.append(String.join(",", Collections.nCopies(pedido.getListStatus().size(), "?")));
                sql.append(") ");

                for (Status status : pedido.getListStatus()) {
                    parametros.add(status.name());
                }
            }
            if (pedido.getTransportadora() != null) {
                if (pedido.getTransportadora().getId() != null) {
                    sql.append(" AND f.fre_id = ? ");
                    parametros.add(pedido.getTransportadora().getId());
                }
                if (isStringValida(pedido.getTransportadora().getNome())) {
                    sql.append(" AND f.fre_nome = ? ");
                    parametros.add(pedido.getTransportadora().getNome());
                }
            }
        }
        if(pedidoProduto.getProduto() != null){
            Produto produto = pedidoProduto.getProduto();
            if(produto.getId() != null){
                sql.append(" AND t.ten_id=? ");
                parametros.add(produto.getId());
            }
            if(isStringValida(produto.getSku())){
                sql.append(" AND t.ten_sku =? ");
                parametros.add(produto.getSku());
            }
            if(isStringValida(produto.getCor())){
                sql.append(" AND t.ten_cor =? ");
                parametros.add(produto.getCor());
            }
            if(isStringValida(produto.getNome())){
                sql.append(" AND LOWER(t.ten_nome) LIKE? ");
                parametros.add("%" + produto.getNome().toLowerCase() + "%");
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
            if (produto.getAtivo() != null) {
                sql.append(" AND t.ten_ativo = ? ");
                parametros.add(produto.getAtivo());
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
        }

        return sql.toString();
    }

    private boolean isStringValida(String value) {
        return value != null && !value.isBlank();
    }

    private PedidoProduto mapeiaPedidoProduto(ResultSet rs) throws SQLException {
        PedidoProduto pp = new PedidoProduto();
        pp.setId(rs.getInt("ped_ten_id"));
        pp.setQuantidade(rs.getInt("ped_ten_quantidade"));

        Pedido ped = new Pedido();
        ped.setId(rs.getInt("ped_id"));
        ped.setValorTotal(rs.getDouble("ped_valor_total"));
        ped.setStatus(Status.valueOf(rs.getString("ped_status")));
        ped.setDtCadastro(rs.getTimestamp("ped_dt_cadastro"));

        Cliente cli = new Cliente();
        cli.setId(rs.getInt("ped_cli_end_cli_id"));

        ClienteEndereco ce = new ClienteEndereco();
        ce.setCliente(cli);

        ped.setClienteEndereco(ce);

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
        pro.setCaminhoFoto(rs.getString("ten_foto"));

        pp.setProduto(pro);
        pp.setPedido(ped);

        return pp;
    }
}
