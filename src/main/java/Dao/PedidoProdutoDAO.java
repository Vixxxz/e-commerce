package Dao;

import Dominio.EntidadeDominio;
import Dominio.Pedido;
import Dominio.PedidoProduto;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
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
        return null;
    }
}
