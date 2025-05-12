package Dao;

import Dominio.*;
import Enums.Status;
import Enums.TipoCupom;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TrocaSolicitadaDAO implements IDAO {
    private Connection connection;

    public TrocaSolicitadaDAO(Connection connection) {
        this.connection = connection;
    }

    public TrocaSolicitadaDAO() {
    }

    public Resultado<TrocaSolicitada> salvarTroca(TrocaSolicitada trocaSolicitada, List<TrocaSolicitadaTenis> trocaSolicitadaTenis) throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        try {
            CupomDAO cupomDAO = new CupomDAO();
            Cupom cupomFiltro = new Cupom();
            cupomFiltro.setPedido(trocaSolicitada.getPedido());
            Resultado<List<EntidadeDominio>> resultadoCupom = cupomDAO.consultar(cupomFiltro);
            List<EntidadeDominio>cupons = resultadoCupom.getValor();

            double valorPago = calcularValorPagoEfetivo(trocaSolicitadaTenis, cupons);

            trocaSolicitada.setValorTotal(valorPago);

            Resultado<EntidadeDominio> resultadoSalvarTroca = salvar(trocaSolicitada);
            TrocaSolicitada trocaSalva = (TrocaSolicitada) resultadoSalvarTroca.getValor();
            TrocaSolicitadaTenisDAO trocaSolicitadaTenisDAO = new TrocaSolicitadaTenisDAO(connection);
            for (TrocaSolicitadaTenis trocaProduto : trocaSolicitadaTenis) {
                trocaProduto.setTroca(trocaSalva);
                trocaSolicitadaTenisDAO.salvaTrocaProduto(trocaProduto);
            }
            PedidoDAO pedidoDAO = new PedidoDAO(connection);
            Pedido pedido = new Pedido();
            pedido.setId(trocaSalva.getPedido().getId());
            pedido.setStatus(trocaSalva.getStatus());
            Resultado<EntidadeDominio> resultadoAlteraPedido = pedidoDAO.alterar(pedido);

            if (!resultadoAlteraPedido.isSucesso()) {
                return Resultado.erro(resultadoAlteraPedido.getErro());
            }

            System.out.println("Troca Solicitada com sucesso!");
            return Resultado.sucesso(trocaSalva);
        } catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null && !connection.isClosed() && connection.isValid(5)) {
                    connection.rollback();
                    System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                } else {
                    System.err.println("Rollback NÃO efetuado devido a erro: " + e.getMessage());
                }
                return Resultado.erro("Erro ao salvar troca: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Erro durante rollback: " + rollbackEx.getMessage());
                return Resultado.erro("Erro durante rollback: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                if (connection != null && !connection.isClosed() && connection.isValid(5)) connection.close();
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

        TrocaSolicitada trocaSolicitada = (TrocaSolicitada) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO crud_v3.troca_solicitada(tro_ped_id, tro_status, tro_data_solicitacao, tro_cli_id, tro_valor_total) VALUES (?, ?, ?, ?, ?)");

        trocaSolicitada.complementarDtCadastro();
        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, trocaSolicitada.getPedido().getId());
            pst.setString(2, trocaSolicitada.getStatus().name());
            pst.setTimestamp(3, new Timestamp(trocaSolicitada.getDtCadastro().getTime()));
            pst.setInt(4, trocaSolicitada.getCliente().getId());
            pst.setDouble(5, trocaSolicitada.getValorTotal());
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir o cliente.");
                }
                int trocaId = rs.getInt(1);
                trocaSolicitada.setId(trocaId);
            }

            return Resultado.sucesso(trocaSolicitada);
        }


    }

    @Override
    public Resultado<EntidadeDominio> alterar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        TrocaSolicitada trocaSolicitada = (TrocaSolicitada) entidade;

        StringBuilder sql = new StringBuilder();
        List<Object> parametros = new ArrayList<>();

        sql.append("UPDATE crud_v3.troca_solicitada SET ");

        List<String> campos = new ArrayList<>();

        if(trocaSolicitada.getPedido() != null){
            Pedido pedido = trocaSolicitada.getPedido();
            if (pedido.getId() != null) {
                campos.add("tro_ped_id = ?");
                parametros.add(pedido.getId());
            }
        }
        if(trocaSolicitada.getStatus() != null){
            campos.add("tro_status = ?");
            parametros.add(trocaSolicitada.getStatus().name());
        }
        if(trocaSolicitada.getCliente() != null){
            if(trocaSolicitada.getCliente().getId() != null){
                campos.add("tro_cli_id = ?");
                parametros.add(trocaSolicitada.getCliente().getId());
            }
        }
        if(trocaSolicitada.getValorTotal() != null){
            campos.add("tro_valor_total = ?");
            parametros.add(trocaSolicitada.getValorTotal());
        }
        if (campos.isEmpty()) {
            return Resultado.erro("Nenhum campo para atualizar.");
        }

        sql.append(String.join(", ", campos));
        sql.append(" WHERE tro_id = ?");
        parametros.add(trocaSolicitada.getId());

        try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                pst.setObject(i + 1, parametros.get(i));
            }

            int linhasAlteradas = pst.executeUpdate();
            if (linhasAlteradas == 0) {
                return Resultado.erro("Nenhuma linha da tabela pedido foi alterada.");
            }
        }
        return Resultado.sucesso(trocaSolicitada);
    }

    @Override
    public Resultado<String> excluir(EntidadeDominio entidade) {
        try{
            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            TrocaSolicitada trocaSolicitada = (TrocaSolicitada) entidade;
            trocaSolicitada.setStatus(Status.TROCA_RECUSADA);

            StringBuilder sql = new StringBuilder();
            List<Object> parametros = new ArrayList<>();

            sql.append("UPDATE crud_v3.troca_solicitada SET ");

            List<String> campos = new ArrayList<>();

            if(trocaSolicitada.getStatus() != null){
                campos.add("tro_status = ?");
                parametros.add(trocaSolicitada.getStatus().name());
            }

            if (campos.isEmpty()) {
                return Resultado.erro("Nenhum campo para atualizar.");
            }

            sql.append(String.join(", ", campos));
            sql.append(" WHERE tro_id = ?");
            parametros.add(trocaSolicitada.getId());

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                int linhasAlteradas = pst.executeUpdate();
                if (linhasAlteradas == 0) {
                    return Resultado.erro("Nenhum registro alterado");
                }
            }
            connection.commit();
            return Resultado.sucesso("Troca cancelada com sucesso");
        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Erro ao cancelar a troca: " + entidade.getId() + " Erro: " + e.getMessage());
                return Resultado.erro("Erro ao cancelar a troca: " + entidade.getId() + " Erro: " + e.getMessage());
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
    public Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade) {
        try {
            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }

            List<EntidadeDominio> trocasSolicitadas = new ArrayList<>();
            TrocaSolicitada trocaSolicitada = (TrocaSolicitada) entidade;
            List<Object> parametros = new ArrayList<>();

            String sql = construirConsulta(trocaSolicitada, parametros);

            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        trocasSolicitadas.add(mapeiaTroca(rs));
                    }
                }
            }

            return Resultado.sucesso(trocasSolicitadas);

        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Erro ao consultar as trocas: " + e.getMessage());
            return Resultado.erro("Erro ao consultar as trocas: " + e.getMessage());
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

    private String construirConsulta(TrocaSolicitada trocaSolicitada, List<Object> parametros) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * ");
        sql.append("FROM crud_v3.troca_solicitada t ");
        sql.append("INNER JOIN crud_v3.pedido p ON t.tro_ped_id = p.ped_id ");
        sql.append("INNER JOIN crud_v3.cliente c ON t.tro_cli_id = c.cli_id ");
        sql.append("WHERE 1=1 ");

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
        if(trocaSolicitada.getPedido() != null){
            if(trocaSolicitada.getPedido().getId() != null){
                sql.append(" AND p.ped_id = ? ");
                parametros.add(trocaSolicitada.getPedido().getId());
            }
        }

        return sql.toString();
    }

    private TrocaSolicitada mapeiaTroca(ResultSet rs) throws SQLException {
        TrocaSolicitada tro = new TrocaSolicitada();
        tro.setId(rs.getInt("tro_id"));
        tro.setValorTotal(rs.getDouble("tro_valor_total"));
        tro.setStatus(Status.valueOf(rs.getString("tro_status")));
        tro.setDtCadastro(rs.getTimestamp("tro_data_solicitacao"));

        Pedido ped = new Pedido();
        ped.setId(rs.getInt("tro_ped_id"));
        tro.setPedido(ped);

        Cliente cli = new Cliente();
        cli.setId(rs.getInt("cli_id"));
        cli.setCpf(rs.getString("cli_cpf"));
        tro.setCliente(cli);

        return tro;
    }

    private Double calcularValorPagoEfetivo(List<TrocaSolicitadaTenis> trocaSolicitadaTenis, List<EntidadeDominio> cupons) {
        // Calcula o total de cupons de troca disponíveis
        double totalCupomTroca = cupons.stream()
                .filter(c -> c instanceof Cupom)
                .map(c -> (Cupom) c)
                .filter(c -> TipoCupom.TROCA.equals(c.getTipo()))
                .mapToDouble(Cupom::getValor)
                .sum();

        // Calcula o total de itens sendo trocados
        int totalItens = trocaSolicitadaTenis.stream()
                .mapToInt(TrocaSolicitadaTenis::getQuantidade)
                .sum();

        if (totalItens == 0) return 0.0;

        // Calcula o desconto unitário proporcional
        double descontoUnitario = totalCupomTroca / totalItens;

        // Calcula o valor total pago aplicando o desconto unitário a cada item
        double valorTotal = trocaSolicitadaTenis.stream()
                .mapToDouble(t -> {
                    double valorItem = t.getProduto().getPreco() * t.getQuantidade();
                    double descontoItem = descontoUnitario * t.getQuantidade();
                    return valorItem - descontoItem;
                })
                .sum();

        return Math.max(valorTotal, 0.0);
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
