package Dao;

import Dominio.*;
import Util.Resultado;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PedidoDAO implements IDAO{
    private Connection connection;

    public PedidoDAO(Connection connection) {
        this.connection = connection;
    }

    public PedidoDAO() {
    }

    //todo: fazer a movimentacao do estoque
    public Resultado<Pedido> salvarPedidoEProduto(Pedido pedido, List<PedidoProduto> pedidoProdutos) {
        try {
            Resultado<EntidadeDominio> resultadoSalvarPedido = salvar(pedido);
            Pedido pedidoSalvo = (Pedido) resultadoSalvarPedido.getValor();
            PedidoProdutoDAO pedidoProdutoDAO = new PedidoProdutoDAO(connection);
            for (PedidoProduto pedidoProduto : pedidoProdutos) {
                pedidoProduto.setPedido(pedidoSalvo);
                pedidoProdutoDAO.salvar(pedidoProduto);
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
        return null;
    }
}
