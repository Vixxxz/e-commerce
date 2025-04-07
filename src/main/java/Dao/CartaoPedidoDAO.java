package Dao;

import Dominio.CartaoPedido;
import Dominio.EntidadeDominio;
import Dominio.Estoque;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.List;

public class CartaoPedidoDAO implements IDAO{
    private Connection connection;

    public CartaoPedidoDAO(Connection connection) {
        this.connection = connection;
    }

    public CartaoPedidoDAO() {
    }

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        CartaoPedido cartaoPedido = (CartaoPedido) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO cartao_pedido(car_ped_car_id, car_ped_ped_id, car_ped_dt_cadastro) ");
        sql.append("VALUES (?,?,?)");

        cartaoPedido.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, cartaoPedido.getCartao().getId());
            pst.setInt(2, cartaoPedido.getPedido().getId());
            pst.setDate(3, new Date(cartaoPedido.getDtCadastro().getTime()));
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir o cartao_pedido.");
                }
                int idCartaoPedido = rs.getInt(1);
                cartaoPedido.setId(idCartaoPedido);
            }
            return Resultado.sucesso(cartaoPedido);
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
