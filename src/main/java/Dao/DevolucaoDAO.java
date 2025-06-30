package Dao;

import Dominio.*;
import Enums.Status;
import Enums.TipoCupom;
import Util.Conexao;
import Util.CupomGenerator;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DevolucaoDAO implements IDAO{
    private Connection connection;

    public DevolucaoDAO(Connection connection) {
        this.connection = connection;
    }

    public DevolucaoDAO() {
    }

    public Resultado<Devolucao>salvaDevolucaoCupom(Devolucao devolucao, List<DevolucaoProduto> produtos)  {
        try{
            PedidoDAO pedidoDAO = new PedidoDAO();
            Pedido pedido = buscarPedidoValido(pedidoDAO, devolucao.getPedido());
            if (pedido == null) return Resultado.erro("Pedido não existente");

            CupomDAO cupomDAO = new CupomDAO();
            Cupom cupFiltro = new Cupom();
            cupFiltro.setPedido(pedido);
            Resultado<List<EntidadeDominio>> resultadoCupom = cupomDAO.consultar(cupFiltro);
            List<EntidadeDominio> cupons = resultadoCupom.getValor();

            TrocaSolicitadaDAO trocaDAO = new TrocaSolicitadaDAO();
            TrocaSolicitada troca = buscarTrocaValida(trocaDAO, pedido);
            if (troca == null) return Resultado.erro("Nenhuma troca solicitada para esse pedido");

            troca.setStatus(Status.TROCADO);
            pedido.setStatus(Status.TROCADO);

            Resultado<EntidadeDominio> resDevolucao = salvar(devolucao);
            if (!resDevolucao.isSucesso()) return Resultado.erro("Erro ao salvar devolução");

            Devolucao dev = (Devolucao) resDevolucao.getValor();

            DevolucaoProdutoDAO devProdutoDAO = new DevolucaoProdutoDAO();
            devProdutoDAO.setConnection(connection);
            EstoqueDAO estoqueDAO = new EstoqueDAO(connection);

            for (DevolucaoProduto produto : produtos) {
                produto.setDevolucao(dev);
                Cupom cupom = gerarCupom(calcularValorPagoEfetivo(produto, produtos, cupons), pedido);
                Resultado<EntidadeDominio> resProdCupom = devProdutoDAO.salvaDevolucaoProdutoCupom(produto, cupom);
                Resultado<EntidadeDominio> resEstoque = estoqueDAO.atualizarEstoque(produto);
            }

            trocaDAO.setConnection(connection);
            Resultado<EntidadeDominio> resTroca = trocaDAO.alterar(troca);

            pedidoDAO.setConnection(connection);
            Resultado<EntidadeDominio> resPedido = pedidoDAO.alterar(pedido);

            return Resultado.sucesso(dev);
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
        try {
            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }

            List<EntidadeDominio> devolucoesSolicitadas = new ArrayList<>();
            Devolucao devolucao = (Devolucao) entidade;
            List<Object> parametros = new ArrayList<>();

            String sql = construirConsulta(devolucao, parametros);

            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        devolucoesSolicitadas.add(mapeiaDevolucao(rs));
                    }
                }
            }

            return Resultado.sucesso(devolucoesSolicitadas);

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Erro ao consultar as devolucoes: " + e.getMessage());
            return Resultado.erro("Erro ao consultar as devolucoes: " + e.getMessage());
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar conexão: " + closeEx.getMessage());
            }
        }
    }

    private String construirConsulta(Devolucao devolucao, List<Object> parametros) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * ");
        sql.append("FROM crud_v3.devolucao d ");
        sql.append("INNER JOIN crud_v3.pedido p ON d.dev_ped_id = p.ped_id ");
        sql.append("WHERE 1=1 ");

        if (devolucao.getId() != null) {
            sql.append(" AND d.dev_id = ? ");
            parametros.add(devolucao.getId());
        }
        if (devolucao.getValor() != null) {
            sql.append(" AND d.dev_valor = ? ");
            parametros.add(devolucao.getValor());
        }
        if(devolucao.getPedido() != null){
            if(devolucao.getPedido().getId() != null){
                sql.append(" AND p.ped_id = ? ");
                parametros.add(devolucao.getPedido().getId());
            }
        }

        return sql.toString();
    }

    private Devolucao mapeiaDevolucao(ResultSet rs) throws SQLException {
        Devolucao dev = new Devolucao();
        dev.setId(rs.getInt("dev_id"));
        dev.setValor(rs.getDouble("dev_valor"));


        Pedido ped = new Pedido();
        ped.setId(rs.getInt("ped_id"));
        dev.setPedido(ped);

        return dev;
    }

    private Pedido buscarPedidoValido(PedidoDAO dao, Pedido filtro) throws SQLException {
        Resultado<List<EntidadeDominio>> res = dao.consultar(filtro);
        return res.getValor().isEmpty() ? null : (Pedido) res.getValor().getFirst();
    }

    private TrocaSolicitada buscarTrocaValida(TrocaSolicitadaDAO dao, Pedido pedido) throws SQLException {
        TrocaSolicitada filtro = new TrocaSolicitada();
        filtro.setPedido(pedido);
        Resultado<List<EntidadeDominio>> res = dao.consultar(filtro);
        return res.getValor().isEmpty() ? null : (TrocaSolicitada) res.getValor().getFirst();
    }

    private Cupom gerarCupom(Double valor, Pedido pedido) {
        Cupom cupom = new Cupom();
        cupom.setCodigo(CupomGenerator.gerarCodigoCupom(4));
        cupom.setValor(valor);
        cupom.setCliente(pedido.getClienteEndereco().getCliente());
        cupom.setTipo(TipoCupom.TROCA);
        return cupom;
    }

    private Double calcularValorPagoEfetivo(DevolucaoProduto devolucaoProduto, List<DevolucaoProduto> todosProdutos, List<EntidadeDominio> cupons) {
        double totalCupomTroca = cupons.stream()
                .filter(c -> c instanceof Cupom)
                .map(c -> (Cupom) c)
                //.filter(c -> TipoCupom.TROCA.equals(c.getTipo()))
                .mapToDouble(Cupom::getValor)
                .sum();

        int totalItens = todosProdutos.stream()
                .mapToInt(DevolucaoProduto::getQuantidade)
                .sum();

        if (totalItens == 0) return devolucaoProduto.getProduto().getPreco();

        double descontoUnitario = totalCupomTroca / totalItens;
        double valorPagoEfetivo = devolucaoProduto.getProduto().getPreco() - descontoUnitario;
        return Math.max(valorPagoEfetivo, 0.0);
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
