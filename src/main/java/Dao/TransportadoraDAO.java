package Dao;

import Dominio.Cliente;
import Dominio.EntidadeDominio;
import Dominio.Transportadora;
import Util.Conexao;
import Util.Resultado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransportadoraDAO implements IDAO{
    private Connection connection;

    public TransportadoraDAO(Connection connection) {
        this.connection = connection;
    }

    public TransportadoraDAO() {
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
        try {
            if (connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }

            List<EntidadeDominio> transportadoras = new ArrayList<>();
            Transportadora transportadora = (Transportadora) entidade;
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM crud_v3.frete f ");
            sql.append("WHERE 1=1 ");

            if (transportadora.getId() != null) {
                sql.append("AND f.fre_id = ? ");
                parametros.add(transportadora.getId());
            }
            if (isStringValida(transportadora.getNome())) {
                sql.append("AND f.fre_transportadora = ? ");
                parametros.add(transportadora.getNome());
            }
            if (transportadora.getValor() != null) {
                sql.append("AND f.fre_valor = ? ");
                parametros.add(transportadora.getValor());
            }

            try (PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i + 1, parametros.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        transportadoras.add(mapeiaCliente(rs));
                    }
                }
            }
            return Resultado.sucesso(transportadoras);
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

    private Transportadora mapeiaCliente(ResultSet rs) throws SQLException {
        Transportadora tra = new Transportadora();
        tra.setId(rs.getInt("fre_id"));
        tra.setNome(rs.getString("fre_transportadora"));
        tra.setValor(Double.valueOf(rs.getString("fre_valor")));

        return tra;
    }
}
