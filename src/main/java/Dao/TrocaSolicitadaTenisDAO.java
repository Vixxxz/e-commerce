package Dao;

import Dominio.*;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrocaSolicitadaTenisDAO implements IDAO{
    private Connection connection;

    public TrocaSolicitadaTenisDAO(Connection connection) {
        this.connection = connection;
    }

    public TrocaSolicitadaTenisDAO(){}

    public static Resultado<EntidadeDominio> salvaTrocaProduto(TrocaSolicitadaTenis trocaProduto) {
        return null;
    }

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {

        if(connection == null || connection.isClosed()){
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);


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
}
