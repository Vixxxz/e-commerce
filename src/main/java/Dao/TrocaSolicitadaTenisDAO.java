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

    public void salvaTrocaProduto(TrocaSolicitadaTenis trocaProduto) throws SQLException, ClassNotFoundException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO troca_solicitada_tenis(tst_tro_id, tst_ten_id, tst_quantidade, tst_data_solicitacao) ");
        sql.append("VALUES (?,?,?,?)");

        trocaProduto.complementarDtCadastro();
        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, trocaProduto.getTroca().getId());
            pst.setInt(2, trocaProduto.getProduto().getId());
            pst.setInt(3, trocaProduto.getQuantidade());
            pst.setTimestamp(4, new Timestamp(trocaProduto.getDtCadastro().getTime()));

            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir o Troca Produto.");
                }
                int idTrocaProduto = rs.getInt(1);
                trocaProduto.setId(idTrocaProduto);
            }
        }
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
