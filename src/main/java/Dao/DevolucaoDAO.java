package Dao;

import Dominio.*;
import Enums.Status;
import Util.Resultado;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DevolucaoDAO implements IDAO{
    private Connection connection;

    public DevolucaoDAO(Connection connection) {
        this.connection = connection;
    }

    public DevolucaoDAO() {
    }

    public Resultado<Devolucao>salvaDevolucaoCupom(Devolucao devolucao, Cupom cupom, List<DevolucaoProduto> devolucaoProdutos) throws SQLException, ClassNotFoundException {
        try{
            PedidoDAO pedidoDAO = new PedidoDAO();
            Resultado<List<EntidadeDominio>> resultadoConsultaPedido = pedidoDAO.consultar(devolucao.getPedido());
            List<EntidadeDominio> pedidos = resultadoConsultaPedido.getValor();

            if(pedidos.isEmpty()){
                return Resultado.erro("Pedido não existente");
            }

            Pedido pedido = (Pedido) pedidos.getFirst();

            pedido.setStatus(Status.TROCA_AUTORIZADA);

            Resultado<EntidadeDominio> resultadoSalvaDevolucao = salvar(devolucao);

            Devolucao dev = (Devolucao) resultadoSalvaDevolucao.getValor();

            DevolucaoProdutoDAO devolucaoProdutoDAO = new DevolucaoProdutoDAO();
            devolucaoProdutoDAO.setConnection(connection);

            for(DevolucaoProduto devolucaoProduto : devolucaoProdutos){
                devolucaoProduto.setDevolucao(dev);
                Resultado<EntidadeDominio> resultadoSalvaDevolucaoProduto = devolucaoProdutoDAO.salvar(devolucaoProduto);
            }

            CupomDAO cupomDAO = new CupomDAO(connection);
            Resultado<EntidadeDominio> resultadoSalvaCupom = cupomDAO.salvar(cupom);

            pedidoDAO.setConnection(connection);
            Resultado<EntidadeDominio> resultadoAlteraPedido = pedidoDAO.alterar(pedido);

            return Resultado.sucesso((Devolucao) resultadoSalvaDevolucao.getValor());
        } catch (SQLException | ClassNotFoundException e) {
            try {
                if (!connection.isClosed() && connection.isValid(5) && connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao gerar cupom e devolução: " + e.getMessage());
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
    public Resultado<EntidadeDominio> alterar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
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

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
