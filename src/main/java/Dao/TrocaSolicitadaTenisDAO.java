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

public class TrocaSolicitadaTenisDAO implements IDAO{
    private Connection connection;

    public TrocaSolicitadaTenisDAO(Connection connection) {
        this.connection = connection;
    }

    public TrocaSolicitadaTenisDAO(){}

    public void salvaTrocaProduto(TrocaSolicitadaTenis trocaProduto) throws SQLException, ClassNotFoundException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO troca_solicitada_tenis(tst_tro_id, tst_ten_id, tst_quantidade, tst_data_solicitacao) ");
        sql.append("VALUES (?,?,?,?)");

        trocaProduto.complementarDtCadastro();
        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, trocaProduto.getTroca().getId());
            pst.setInt(2, trocaProduto.getProduto().getId());
            pst.setInt(3, trocaProduto.getQuantidade());
            pst.setTimestamp(4, new Timestamp(trocaProduto.getDtCadastro().getTime()));

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir o Troca Produto.");
                }
                int idTrocaProduto = rs.getInt(1);
                trocaProduto.setId(idTrocaProduto);
            }
        }
    }

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {

        if(connection == null || connection.isClosed()){
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);


        return null;
    }

    @Override
    public Resultado<EntidadeDominio> alterar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
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

            List<EntidadeDominio> trocasTenis = new ArrayList<>();
            TrocaSolicitadaTenis trocaTenis = (TrocaSolicitadaTenis) entidade;
            List<Object> parametros = new ArrayList<>();

            String sql = construirConsulta(trocaTenis, parametros);

            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        trocasTenis.add(mapeiaTrocaTenis(rs));
                    }
                }
            }

            return Resultado.sucesso(trocasTenis);

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Erro ao consultar os produtos da troca: " + e.getMessage());
            return Resultado.erro("Erro ao consultar os produtos da troca: " + e.getMessage());
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

    private String construirConsulta(TrocaSolicitadaTenis trocaTenis, List<Object> parametros) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * ");
        sql.append("FROM crud_v3.troca_solicitada_tenis tst ");
        sql.append("INNER JOIN crud_v3.troca_solicitada t ON tst.tst_tro_id = t.tro_id ");
        sql.append("INNER JOIN crud_v3.tenis te ON tst.tst_ten_id = te.ten_id ");
        sql.append("WHERE 1=1 ");

        if(trocaTenis.getId() != null){
            sql.append(" AND tst.tst_id = ? ");
            parametros.add(trocaTenis.getId());
        }

        if(trocaTenis.getQuantidade() != null){
            sql.append(" AND tst.tst_quantidade = ? ");
            parametros.add(trocaTenis.getQuantidade());
        }

        if(trocaTenis.getProduto() != null){
            Produto produto = trocaTenis.getProduto();
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

        if(trocaTenis.getTroca() != null){
            TrocaSolicitada trocaSolicitada = trocaTenis.getTroca();
            if (trocaSolicitada.getId() != null) {
                sql.append(" AND t.tro_id = ? ");
                parametros.add(trocaSolicitada.getId());
            }
            if (trocaSolicitada.getValorTotal() != null) {
                sql.append(" AND t.tro_valor_total = ? ");
                parametros.add(trocaSolicitada.getValorTotal());
            }
            if (trocaSolicitada.getStatus() != null) {
                sql.append(" AND t.tro_status = ? ");
                parametros.add(trocaSolicitada.getStatus().name());
            }
            if (trocaSolicitada.getListStatus() != null && !trocaSolicitada.getListStatus().isEmpty()) {
                sql.append(" AND p.ped_status IN (");
                sql.append(String.join(",", Collections.nCopies(trocaSolicitada.getListStatus().size(), "?")));
                sql.append(") ");

                for (Status status : trocaSolicitada.getListStatus()) {
                    parametros.add(status.name());
                }
            }
        }

        return sql.toString();
    }

    private boolean isStringValida(String value) {
        return value != null && !value.isBlank();
    }

    private TrocaSolicitadaTenis mapeiaTrocaTenis(ResultSet rs) throws SQLException {
        TrocaSolicitadaTenis tst = new TrocaSolicitadaTenis();
        tst.setId(rs.getInt("tst_id"));
        tst.setQuantidade(rs.getInt("tst_quantidade"));

        TrocaSolicitada tro = new TrocaSolicitada();
        tro.setId(rs.getInt("tro_id"));
        tro.setValorTotal(rs.getDouble("tro_valor_total"));
        tro.setStatus(Status.valueOf(rs.getString("tro_status")));
        tro.setDtCadastro(rs.getTimestamp("tro_data_solicitacao"));

        Pedido ped = new Pedido();
        ped.setId(rs.getInt("tro_ped_id"));
        tro.setPedido(ped);

        Cliente cli = new Cliente();
        cli.setId(rs.getInt("tro_cli_id"));
        tro.setCliente(cli);

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

        tst.setTroca(tro);
        tst.setProduto(pro);

        return tst;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
