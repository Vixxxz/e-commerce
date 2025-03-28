package Dao;

import Dominio.EntidadeDominio;
import Dominio.Marca;
import Util.Conexao;
import Util.Resultado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MarcaDAO implements IDAO{
    private Connection connection;

    public MarcaDAO(){}

    public MarcaDAO(Connection connection){
        this.connection = connection;
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
            if(connection == null || connection.isClosed()){
                connection = Conexao.getConnectionMySQL();
            }

            Marca marca = (Marca) entidade;
            List<EntidadeDominio>marcas = new ArrayList<>();
            List<Object>parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder("SELECT * FROM crud_v3.marca m WHERE 1 = 1 ");

            if(marca.getId() != null){
                sql.append("AND m.mar_id=? ");
                parametros.add(marca.getId());
            }
            if(marca.getNome() != null && !marca.getNome().isBlank()){
                sql.append("AND m.mar_nome=? ");
                parametros.add(marca.getNome());
            }

            try(PreparedStatement pst = connection.prepareStatement(sql.toString())){
                for(int i = 0; i < parametros.size(); i++){
                    pst.setObject(i+1, parametros.get(i));
                }

                try(ResultSet rs = pst.executeQuery()){
                    while(rs.next()){
                        Marca m = new Marca();
                        m.setId(rs.getInt("mar_id"));
                        m.setNome(rs.getString("mar_nome"));
                        marcas.add(m);
                    }
                }
            }
            return Resultado.sucesso(marcas);
        }catch (Exception e) {
            System.err.println("Erro ao consultar as marcas: " + e.getMessage());
            return Resultado.erro("Erro ao consultar as marcas: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }
}
