package Dao;

import Dominio.Bandeira;
import Dominio.Cartao;
import Dominio.Cliente;
import Dominio.EntidadeDominio;
import Util.Conexao;
import Util.Resultado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartaoDAO implements IDAO{
    private Connection connection;

    public CartaoDAO (Connection connection){
        this.connection = connection;
    }

    public CartaoDAO(){}

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        try{
            Cartao cartao = (Cartao) entidade;
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO crud_v3.cartao(car_num, car_num_seguranca, car_nome_impresso, ");
            sql.append("car_preferencial, car_cli_id, car_ban_id, car_dt_cadastro) ");
            sql.append("VALUES(?,?,?,?,?,?,?)");

            ClienteDAO clienteDAO = new ClienteDAO();
            BandeiraDAO bandeiraDAO = new BandeiraDAO();

            Resultado<List<EntidadeDominio>>resultadoCliente = clienteDAO.consultar(cartao.getCliente());
            Resultado<List<EntidadeDominio>>resultadoBandeira = bandeiraDAO.consultar(cartao.getBandeira());

            if (!resultadoCliente.isSucesso()) {
                return Resultado.erro(resultadoCliente.getErro());
            }

            List<EntidadeDominio> clientes = resultadoCliente.getValor();
            List<EntidadeDominio> bandeiras = resultadoBandeira.getValor();

            if (clientes.isEmpty()) {
                return Resultado.erro("Cliente fornecido não está cadastrado");
            }

            cartao.setBandeira((Bandeira) bandeiras.getFirst());
            cartao.setCliente((Cliente) clientes.getFirst());

            Resultado<List<EntidadeDominio>> resultadoCartoes = consultar(cartao);
            if(!resultadoCartoes.isSucesso()){
                return Resultado.erro(resultadoCartoes.getErro());
            }
            List<EntidadeDominio> cartoes = resultadoCartoes.getValor();

            if(!cartoes.isEmpty()){
                return Resultado.erro("Cartao ja cadastrada no sistema");
            }

            if(connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            cartao.complementarDtCadastro();

            try(PreparedStatement pst = connection.prepareStatement(sql.toString(), PreparedStatement.RETURN_GENERATED_KEYS)){
                pst.setString(1, cartao.getNumero());
                pst.setString(2, cartao.getNumSeguranca());
                pst.setString(3, cartao.getNomeImpresso());
                pst.setBoolean(4, cartao.getPreferencial());
                pst.setInt(5, cartao.getCliente().getId());
                pst.setInt(6, cartao.getBandeira().getId());
                pst.setTimestamp(7, new java.sql.Timestamp(cartao.getDtCadastro().getTime()));

                pst.executeUpdate();

                try(ResultSet rs = pst.getGeneratedKeys()){
                    if(rs.next()){
                        System.out.println(rs.getInt(1));
                        int idCartao = rs.getInt(1);
                        cartao.setId(idCartao);
                    }
                }
            }
            connection.commit();
            return Resultado.sucesso(cartao);
        } catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao salvar cartao: " + e.getMessage());
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
    public Resultado<EntidadeDominio> alterar(EntidadeDominio entidade) {
        try{
            Cartao cartao = (Cartao) entidade;
            Cartao carTeste = new Cartao();
            carTeste.setId(cartao.getId());
            Resultado<List<EntidadeDominio>> resultadoBandeiras = consultar(carTeste);
            if(!resultadoBandeiras.isSucesso()){
                return Resultado.erro(resultadoBandeiras.getErro());
            }
            List<EntidadeDominio> cartoes = resultadoBandeiras.getValor();

            if(cartoes.isEmpty()){
                return Resultado.erro("Cartao não cadastrada no sistema");
            }



            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE crud_v3.cartao SET ");
            sql.append("car_num = ?, car_num_seguranca = ?, car_nome_impresso = ?, ");
            sql.append("car_preferencial = ?, car_cli_id = ?, car_ban_id = ? ");
            sql.append("WHERE car_id = ?");

            try(PreparedStatement pst = connection.prepareStatement(sql.toString())){
                pst.setString(1, cartao.getNumero());
                pst.setString(2, cartao.getNumSeguranca());
                pst.setString(3, cartao.getNomeImpresso());
                pst.setBoolean(4, cartao.getPreferencial());
                pst.setInt(5, cartao.getCliente().getId());
                pst.setInt(6, cartao.getBandeira().getId());
                pst.setInt(7, cartao.getId());

                int rowsUpdated = pst.executeUpdate();
                if (rowsUpdated == 0) {
                    return Resultado.erro("Nenhum cartao foi alterado");
                }
            }
            connection.commit();
            return Resultado.sucesso(cartao);
        } catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao alterar cartao: " + e.getMessage());
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
    public Resultado<String> excluir(EntidadeDominio entidade) {
        try{
            Cartao cartao = (Cartao) entidade;
            Resultado<List<EntidadeDominio>> resultadoCartao = consultar(cartao);
            List<EntidadeDominio> listCartao = resultadoCartao.getValor();

            if(listCartao.isEmpty()) {
                return Resultado.erro("Cartao não cadastrado no sistema");
            }

            cartao = (Cartao) listCartao.getFirst();

            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM crud_v3.cartao WHERE car_id = ?");

            try(PreparedStatement pst = connection.prepareStatement(sql.toString())){
                pst.setInt(1, cartao.getId());
                pst.executeUpdate();
            }
            connection.commit();
            return Resultado.sucesso("Cartao excluido com sucesso");
        } catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao alterar bandeira: " + e.getMessage());
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

            List<EntidadeDominio> cartoes = new ArrayList<>();
            Cartao cartao = (Cartao) entidade;
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM crud_v3.cartao c ");
            sql.append("INNER JOIN crud_v3.bandeira b on c.car_ban_id = b.ban_id ");
            sql.append("INNER JOIN crud_v3.cliente cl on c.car_cli_id = cl.cli_id ");
            sql.append("WHERE 1=1 ");

            if (cartao.getId() != null) {
                sql.append("AND c.car_id = ? ");
                parametros.add(cartao.getId());
            }
            if (isStringValida(cartao.getNumero())) {
                sql.append("AND c.car_num = ? ");
                parametros.add(cartao.getNumero());
            }
            if (isStringValida(cartao.getNumSeguranca())) {
                sql.append("AND c.car_num_seguranca = ? ");
                parametros.add(cartao.getNumSeguranca());
            }
            if (isStringValida(cartao.getNomeImpresso())) {
                sql.append("AND c.car_nome_impresso = ? ");
                parametros.add(cartao.getNomeImpresso());
            }
            if (cartao.getPreferencial() != null) {
                sql.append("AND c.car_preferencial = ? ");
                parametros.add(cartao.getPreferencial());
            }
            if(cartao.getBandeira() != null) {
                if (cartao.getBandeira().getId() != null) {
                    sql.append("AND c.car_ban_id = ? ");
                    parametros.add(cartao.getBandeira().getId());
                }
                if (isStringValida(cartao.getBandeira().getNomeBandeira())) {
                    sql.append("AND b.ban_nome = ? ");
                    parametros.add(cartao.getBandeira().getNomeBandeira());
                }
            }
            if(cartao.getCliente() != null){
                if (cartao.getCliente().getId() != null) {
                    sql.append("AND cl.cli_id = ? ");
                    parametros.add(cartao.getCliente().getId());
                }
                if (isStringValida(cartao.getCliente().getCpf())) {
                    sql.append("AND cl.cli_cpf = ? ");
                    parametros.add(cartao.getCliente().getCpf());
                }
            }

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        cartoes.add(mapeiaCartao(rs));
                    }
                }
            }
            return Resultado.sucesso(cartoes);
        } catch (Exception e) {
            System.err.println("Erro ao consultar clientes: " + e.getMessage());
            return Resultado.erro("Erro ao consultar clientes: " + e.getMessage());
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

    private Cartao mapeiaCartao(ResultSet rs) throws SQLException {
        Cartao car = new Cartao();
        car.setNumero(rs.getString("car_num"));
        car.setNomeImpresso(rs.getString("car_nome_impresso"));
        car.setNumSeguranca(rs.getString("car_num_seguranca"));
        car.setPreferencial(rs.getBoolean("car_preferencial"));
        car.setId(rs.getInt("car_id"));

        Bandeira ban = new Bandeira();
        ban.setId(rs.getInt("ban_id"));
        ban.setNomeBandeira(rs.getString("ban_nome"));
        car.setBandeira(ban);

        Cliente cli = new Cliente();
        cli.setId(rs.getInt("cli_id"));
        cli.setCpf(rs.getString("cli_cpf"));
        car.setCliente(cli);

        return car;
    }
}
