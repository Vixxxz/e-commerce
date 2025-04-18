package Dao;

import Dominio.*;
import Enums.Status;
import Enums.TipoCupom;
import Util.Conexao;
import Util.Resultado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CupomDAO implements IDAO{
    private Connection connection;

    public CupomDAO(Connection connection) {
        this.connection = connection;
    }

    public CupomDAO() {
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
        try{
            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }

            List<EntidadeDominio> cupons = new ArrayList<>();
            Cupom cupom = (Cupom) entidade;
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM crud_v3.cupom c ");
            sql.append("LEFT JOIN crud_v3.cliente cli ON c.cup_cli_id = cli.cli_id ");
            sql.append("LEFT JOIN crud_v3.pedido p ON c.cup_ped_id = p.ped_id ");
            sql.append("WHERE 1=1 ");

            if (cupom.getId() != null) {
                sql.append("AND c.cup_id = ? ");
                parametros.add(cupom.getId());
            }
            if (isStringValida(cupom.getCodigo())) {
                sql.append("AND c.cup_codigo = ? ");
                parametros.add(cupom.getCodigo());
            }
            if (cupom.getValor() != null) {
                sql.append("AND c.cup_valor = ? ");
                parametros.add(cupom.getValor());
            }
            if (cupom.getTipo() != null) {
                sql.append("AND c.cup_tipo = ? ");
                parametros.add(cupom.getTipo());
            }
            if (cupom.getCliente() != null) {
                if(cupom.getCliente().getId() != null) {
                    sql.append("AND c.cup_cli_id = ? ");
                    parametros.add(cupom.getCliente().getId());
                }
                if(cupom.getCliente().getCpf() != null) {
                    sql.append("AND cli.cli_cpf = ? ");
                    parametros.add(cupom.getCliente().getCpf());
                }
            }
            if (cupom.getPedido() != null) {
                if(cupom.getPedido().getId() != null) {
                    sql.append("AND c.cup_ped_id = ? ");
                    parametros.add(cupom.getPedido().getId());
                }
            }

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        cupons.add(mapeiaCupom(rs));
                    }
                }
            }
            return Resultado.sucesso(cupons);
        } catch (Exception e) {
            System.err.println("Erro ao consultar cupons: " + e.getMessage());
            return Resultado.erro("Erro ao consultar cupons: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }

    private boolean isStringValida(String value) {
        return value != null && !value.isBlank();
    }

    private Cupom mapeiaCupom(ResultSet rs) throws SQLException {
        Cupom cup = new Cupom();
        cup.setId(rs.getInt("cup_id"));
        cup.setCodigo(rs.getString("cup_codigo"));
        cup.setValor(rs.getDouble("cup_valor"));
        cup.setTipo(TipoCupom.valueOf(rs.getString("cup_tipo")));

        Cliente cli = new Cliente();
        cli.setId(rs.getInt("cli_id"));
        cli.setRanking(rs.getString("cli_ranking"));
        cli.setNome(rs.getString("cli_nome"));
        cli.setGenero(rs.getString("cli_genero"));
        cli.setCpf(rs.getString("cli_cpf"));
        cli.setTipoTelefone(rs.getString("cli_tp_tel"));
        cli.setTelefone(rs.getString("cli_tel"));
        cli.setEmail(rs.getString("cli_email"));
        cli.setSenha(rs.getString("cli_senha"));
        cli.setDataNascimento(rs.getDate("cli_dt_nasc"));

        Pedido ped = new Pedido();
        ped.setId(rs.getInt("ped_id"));
        ped.setValorTotal(rs.getDouble("ped_valor_total"));
        ped.setStatus(Status.valueOf(rs.getString("ped_status")));


        cup.setCliente(cli);
        cup.setPedido(ped);

        return cup;
    }
}
