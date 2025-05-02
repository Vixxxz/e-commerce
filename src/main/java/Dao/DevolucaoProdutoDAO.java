package Dao;

import Dominio.Cupom;
import Dominio.DevolucaoProduto;
import Dominio.EntidadeDominio;
import Dominio.Pedido;
import Util.Conexao;
import Util.Resultado;

import java.sql.*;
import java.util.List;

public class DevolucaoProdutoDAO implements IDAO{
    private Connection connection;

    public DevolucaoProdutoDAO(Connection connection) {
        this.connection = connection;
    }

    public DevolucaoProdutoDAO() {
    }

    public Resultado<EntidadeDominio> salvaDevolucaoProdutoCupom(DevolucaoProduto devolucaoProduto, Cupom cupom) throws SQLException, ClassNotFoundException {
        Resultado<EntidadeDominio> resultadoSalvaDevolucaoProduto = salvar(devolucaoProduto);
        CupomDAO cupomDAO = new CupomDAO(connection);
        Resultado<EntidadeDominio> resultadoSalvaCupom = cupomDAO.salvar(cupom);
        return Resultado.sucesso(resultadoSalvaDevolucaoProduto.getValor());
    }

    @Override
    public Resultado<EntidadeDominio> salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            connection = Conexao.getConnectionMySQL();
        }
        connection.setAutoCommit(false);

        DevolucaoProduto devolucaoProduto = (DevolucaoProduto) entidade;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO devolucao_tenis(dev_ten_dev_id, dev_ten_ten_id, dev_ten_quantidade, dev_ten_data_adicao) ");
        sql.append("VALUES (?,?,?,?)");

        devolucaoProduto.complementarDtCadastro();

        try (PreparedStatement pst = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, devolucaoProduto.getDevolucao().getId());
            pst.setInt(2, devolucaoProduto.getProduto().getId());
            pst.setInt(3, devolucaoProduto.getQuantidade());
            pst.setTimestamp(4, new Timestamp(devolucaoProduto.getDtCadastro().getTime()));
            pst.executeUpdate();

            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Falha ao inserir a devolucao e seus produtos.");
                }
                int idDevolucaoProduto = rs.getInt(1);
                devolucaoProduto.setId(idDevolucaoProduto);
            }
            return Resultado.sucesso(devolucaoProduto);
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
        return null;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
