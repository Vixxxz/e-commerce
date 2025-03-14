package Dao;

import Dominio.Bandeira;
import Dominio.Cartao;
import Dominio.EntidadeDominio;
import Util.Conexao;
import Util.Resultado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BandeiraDAO implements IDAO{
    private Connection connection;

    public BandeiraDAO (Connection connection){
        this.connection = connection;
    }

    public BandeiraDAO(){}

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        try{
            Bandeira bandeira = (Bandeira) entidade;
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO crud_v3.bandeira(ban_nome, ban_dt_cadastro) VALUES (?,?)");

            Resultado<List<EntidadeDominio>> resultadoBandeiras = consultar(bandeira);
            if(!resultadoBandeiras.isSucesso()){
                return Resultado.erro(resultadoBandeiras.getErro());
            }
            List<EntidadeDominio> bandeiras = resultadoBandeiras.getValor();

            if(!bandeiras.isEmpty()){
                return Resultado.erro("Bandeira ja cadastrada no sistema");
            }

            if(connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            bandeira.complementarDtCadastro();

            try(PreparedStatement pst = connection.prepareStatement(sql.toString(), PreparedStatement.RETURN_GENERATED_KEYS)){
                pst.setString(1, bandeira.getNomeBandeira());
                pst.setTimestamp(2, new java.sql.Timestamp(bandeira.getDtCadastro().getTime()));

                pst.executeUpdate();

                try(ResultSet rs = pst.getGeneratedKeys()){
                    if(rs.next()){
                        System.out.println(rs.getInt(1));
                        int idBandeira = rs.getInt(1);
                        bandeira.setId(idBandeira);
                    }
                }
            }
            connection.commit();
            return Resultado.sucesso(bandeira);
        } catch (SQLException | ClassNotFoundException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
                System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
                return Resultado.erro("Erro ao salvar bandeira: " + e.getMessage());
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
            Bandeira bandeira = (Bandeira) entidade;
            Bandeira banTeste = new Bandeira();
            banTeste.setId(bandeira.getId());
            Resultado<List<EntidadeDominio>> resultadoBandeiras = consultar(banTeste);
            if(!resultadoBandeiras.isSucesso()){
                return Resultado.erro(resultadoBandeiras.getErro());
            }
            List<EntidadeDominio> bandeiras = resultadoBandeiras.getValor();

            if(bandeiras.isEmpty()){
                return Resultado.erro("Bandeira não cadastrada no sistema");
            }

            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE crud_v3.bandeira SET ");
            sql.append("ban_nome = ? ");
            sql.append("WHERE ban_id = ?");

            try(PreparedStatement pst = connection.prepareStatement(sql.toString())){
                pst.setString(1, bandeira.getNomeBandeira());
                pst.setInt(2, bandeira.getId());

                int rowsUpdated = pst.executeUpdate();
                if (rowsUpdated == 0) {
                    return Resultado.erro("Nenhuma bandeira foi alterada");
                }
            }
            connection.commit();
            return Resultado.sucesso(bandeira);
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
    public Resultado<String> excluir(EntidadeDominio entidade) {
        try{
            Bandeira bandeira = (Bandeira) entidade;
            Resultado<List<EntidadeDominio>> resultadoBandeira = consultar(bandeira);
            List<EntidadeDominio> listBandeira = resultadoBandeira.getValor();

            if(listBandeira.isEmpty()) {
                return Resultado.erro("Bandeira não cadastrada no sistema");
            }

            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }
            connection.setAutoCommit(false);

            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM crud_v3.bandeira WHERE ban_id = ?");

            CartaoDAO cartaoDAO = new CartaoDAO();
            Cartao cartao = new Cartao();
            cartao.setBandeira(bandeira);
            cartaoDAO.excluir(cartao);

            try(PreparedStatement pst = connection.prepareStatement(sql.toString())){
                pst.setInt(1, bandeira.getId());
                pst.executeUpdate();
            }
            connection.commit();
            return Resultado.sucesso("Bandeira excluido com sucesso");
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

            Bandeira bandeira = (Bandeira) entidade;

            List<EntidadeDominio> bandeiras = new ArrayList<>();
            List<Object> parametros = new ArrayList<>();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM crud_v3.bandeira b WHERE 1=1");

            if(bandeira.getId()!= null){
                sql.append(" AND b.ban_id = ? ");
                parametros.add(bandeira.getId());
            }

            if(bandeira.getNomeBandeira()!= null &&!bandeira.getNomeBandeira().isBlank()){
                sql.append(" AND b.ban_nome = ? ");
                parametros.add(bandeira.getNomeBandeira());
            }
            try(PreparedStatement pst = connection.prepareStatement(sql.toString())){
                for(int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i+1, parametros.get(i));
                }

                try(ResultSet rs = pst.executeQuery()){
                    while(rs.next()) {
                        Bandeira ban = new Bandeira();
                        ban.setId(rs.getInt("ban_id"));
                        ban.setNomeBandeira(rs.getString("ban_nome"));
                        bandeiras.add(ban);
                    }
                }
            }
            return Resultado.sucesso(bandeiras);
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Rollback efetuado devido a erro: " + e.getMessage());
            return Resultado.erro("Erro ao consultar bandeira: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }
}
