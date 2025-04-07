package Dao;

import Dominio.*;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.List;

public class PedidoDAO implements IDAO{
    private Connection connection;

    public PedidoDAO(Connection connection) {
        this.connection = connection;
    }

    public PedidoDAO() {
    }

    //todo: fazer a movimentacao do estoque
    public Resultado<Pedido> salvarPedidoEProduto(Pedido pedido, List<PedidoProduto> pedidoProdutos, List<CartaoPedido> cartaoPedidos) {
        try {
            Resultado<EntidadeDominio> resultadoSalvarPedido = salvar(pedido);
            Pedido pedidoSalvo = (Pedido) resultadoSalvarPedido.getValor();
            PedidoProdutoDAO pedidoProdutoDAO = new PedidoProdutoDAO(connection);
            for (PedidoProduto pedidoProduto : pedidoProdutos) {
                pedidoProduto.setPedido(pedidoSalvo);
                Resultado<EntidadeDominio> resultadoPedidoProduto = pedidoProdutoDAO.salvar(pedidoProduto);
                if(!resultadoPedidoProduto.isSucesso()){
                    return Resultado.erro("Erro ao salvar pedido de produto: " + resultadoPedidoProduto.getErro());
                }
            }
            CartaoPedidoDAO cartaoPedidoDAO = new CartaoPedidoDAO(connection);
            for(CartaoPedido cartaoPedido : cartaoPedidos) {
                cartaoPedido.setPedido(pedidoSalvo);
                Resultado<EntidadeDominio> resultadoCartaoPedido = cartaoPedidoDAO.salvar(cartaoPedido);
                if(!resultadoCartaoPedido.isSucesso()) {
                    return Resultado.erro("Erro ao salvar cartão de pedido: " + resultadoCartaoPedido.getErro());
                }
            }
            EstoqueDAO estoqueDAO = new EstoqueDAO(connection);
            for (PedidoProduto pedidoProduto : pedidoProdutos) {
                Resultado<EntidadeDominio>resultadoAtualizaEstoque = estoqueDAO.atualizarEstoque(pedidoProduto);
                if(!resultadoAtualizaEstoque.isSucesso()) {
                    return Resultado.erro("Erro ao atualizar estoque: " + resultadoAtualizaEstoque.getErro());
                }
            }
            System.out.println("Pedido salvo com sucesso!");
            connection.commit();
            return Resultado.sucesso(pedidoSalvo);
        } catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao salvar pedido: " + e.getMessage());
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
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        Pedido pedido = (Pedido) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO pedido(ped_valor_total, ped_status, ped_dt_cadastro, ped_fre_id, ");
        sql.append("ped_cli_end_id, ped_cli_end_cli_id, ped_cli_end_end_id) ");
        sql.append("VALUES (?,?,?,?,?,?,?)");

        pedido.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setDouble(1, pedido.getValorTotal());
            pst.setString(2, pedido.getStatus().name());
            pst.setDate(3, new Date(pedido.getDtCadastro().getTime()));
            pst.setInt(4, pedido.getTransportadora().getId());
            pst.setInt(5, pedido.getClienteEndereco().getId());
            pst.setInt(6, pedido.getClienteEndereco().getCliente().getId());
            pst.setInt(7, pedido.getClienteEndereco().getEndereco().getId());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir o pedido.");
                }
                int idPedido = rs.getInt(1);
                pedido.setId(idPedido);
            }
            return Resultado.sucesso(pedido);
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
