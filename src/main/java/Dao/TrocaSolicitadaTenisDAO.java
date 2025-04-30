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

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {

        if(connection == null || connection.isClosed()){
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);


        return null;
    }
}
