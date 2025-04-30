package Dao;

import Dominio.EntidadeDominio;
import Util.Resultado;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DevolucaoProdutoDAO implements IDAO{
    private Connection connection;

    public DevolucaoProdutoDAO(Connection connection) {
        this.connection = connection;
    }

    public DevolucaoProdutoDAO() {
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
