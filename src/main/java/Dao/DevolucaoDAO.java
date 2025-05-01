package Dao;

import Dominio.*;
import Enums.Status;
import Enums.TipoCupom;
import Util.Conexao;
import Util.CupomGenerator;
import Util.Resultado;

import java.sql.*;
import java.util.List;

public class DevolucaoDAO implements IDAO{
    private Connection connection;

    public DevolucaoDAO(Connection connection) {
        this.connection = connection;
    }

    public DevolucaoDAO() {
    }

    //todo: realizar a geracao de cupons
    public Resultado<Devolucao>salvaDevolucaoCupom(Devolucao devolucao, List<DevolucaoProduto> devolucaoProdutos)  {
        try{
            PedidoDAO pedidoDAO = new PedidoDAO();
            Resultado<List<EntidadeDominio>> resultadoConsultaPedido = pedidoDAO.consultar(devolucao.getPedido());
            List<EntidadeDominio> pedidos = resultadoConsultaPedido.getValor();

            if(pedidos.isEmpty()){
                return Resultado.erro("Pedido não existente");
            }

            Pedido pedido = (Pedido) pedidos.getFirst();

            TrocaSolicitadaDAO trocaSolicitadaDAO = new TrocaSolicitadaDAO();
            TrocaSolicitada trocaSolicitada = new TrocaSolicitada();
            trocaSolicitada.setPedido(pedido);
            Resultado<List<EntidadeDominio>> resultadoConsultaTroca = trocaSolicitadaDAO.consultar(trocaSolicitada);
            List<EntidadeDominio> trocas = resultadoConsultaTroca.getValor();

            if(trocas.isEmpty()){
                return Resultado.erro("Nenhuma troca solicitada para esse pedido");
            }

            trocaSolicitada = (TrocaSolicitada) trocas.getFirst();

            pedido.setStatus(Status.TROCADO);
            trocaSolicitada.setStatus(Status.TROCADO);

            Resultado<EntidadeDominio> resultadoSalvaDevolucao = salvar(devolucao);

            Devolucao dev = (Devolucao) resultadoSalvaDevolucao.getValor();

            DevolucaoProdutoDAO devolucaoProdutoDAO = new DevolucaoProdutoDAO();
            devolucaoProdutoDAO.setConnection(connection);

            EstoqueDAO estoqueDAO = new EstoqueDAO(connection);

            for(DevolucaoProduto devolucaoProduto : devolucaoProdutos){
                devolucaoProduto.setDevolucao(dev);
                Cupom cupom = new Cupom();
                cupom.setCodigo(CupomGenerator.gerarCodigoCupom(4));
                cupom.setValor(devolucao.getValor());
                cupom.setCliente(pedido.getClienteEndereco().getCliente());
                cupom.setTipo(TipoCupom.TROCA);
                Resultado<EntidadeDominio> resultadoSalvaDevolucaoProduto = devolucaoProdutoDAO.salvaDevolucaoProdutoCupom(devolucaoProduto, cupom);
                Resultado<EntidadeDominio> atualizaEstoque = estoqueDAO.atualizarEstoque(devolucaoProduto);
            }

            trocaSolicitadaDAO.setConnection(connection);
            Resultado<EntidadeDominio>resultadoAlteraTroca = trocaSolicitadaDAO.alterar(trocaSolicitada);

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
        if (connection == null || connection.isClosed()) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        Devolucao devolucao = (Devolucao) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO devolucao(dev_ped_id, dev_valor, dev_data_devolucao) ");
        sql.append("VALUES (?,?,?)");

        devolucao.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, devolucao.getPedido().getId());
            pst.setDouble(2, devolucao.getValor());
            pst.setTimestamp(3, new Timestamp(devolucao.getDtCadastro().getTime()));
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir a devolucao.");
                }
                int idDevolucao = rs.getInt(1);
                devolucao.setId(idDevolucao);
            }
            return Resultado.sucesso(devolucao);
        }
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
