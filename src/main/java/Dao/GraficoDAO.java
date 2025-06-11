package Dao;

import Dominio.*;
import Util.Conexao;
import Util.Resultado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GraficoDAO implements IDAO{
    private Connection connection;

    public GraficoDAO() {
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
        try {
            if(connection == null || connection.isClosed()) {
                connection = Conexao.getConnectionMySQL();
            }

            Grafico grafico = (Grafico) entidade;
            List<EntidadeDominio> dados = new ArrayList<>();
            List<Object> parametros = new ArrayList<>();

            StringBuilder sql = new StringBuilder(
                    "SELECT c.cat_nome, ABS(SUM(e.est_movimentacao)) as vendas, " +
                            "DATE_FORMAT(e.est_dt_cadastro, '%Y-%m') AS mes_ano " +
                            "FROM crud_v3.estoque e "
            );
            sql.append("INNER JOIN crud_v3.tenis t ON t.ten_id = e.est_ten_id ");
            sql.append("INNER JOIN crud_v3.categoria c ON c.cat_id = t.ten_cat_id ");
            sql.append("WHERE e.est_movimentacao < 0 ");

            if(grafico.getDataInicio() != null) {
                sql.append("AND e.est_dt_cadastro >= ? ");
                parametros.add(grafico.getDataInicio());
            }
            if(grafico.getDataFim() != null) {
                sql.append("AND e.est_dt_cadastro <= ? ");
                parametros.add(grafico.getDataFim());
            }

            sql.append("GROUP BY c.cat_nome, DATE_FORMAT(e.est_dt_cadastro, '%Y-%m') ");
            sql.append("ORDER BY c.cat_nome, mes_ano");

            try(PreparedStatement pst = connection.prepareStatement(sql.toString())) {
                for(int i = 0; i < parametros.size(); i++) {
                    pst.setObject(i+1, parametros.get(i));
                }

                try(ResultSet rs = pst.executeQuery()) {
                    while(rs.next()) {
                        dados.add(mapeiaGrafico(rs));
                    }
                }
            }

//            Resultado<Grafico> resultado = buscarMesesIntervalo(grafico);
//            dados.add(resultado.getValor());
            return Resultado.sucesso(dados);

        } catch (Exception e) {
            System.err.println("Erro ao consultar o estoque para gerar o grafico: " + e.getMessage());
            return Resultado.erro("Erro ao consultar o estoque para gerar o grafico: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException closeEx) {
                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
            }
        }
    }

    private Grafico mapeiaGrafico(ResultSet rs) throws SQLException {
        Grafico gra = new Grafico();
        gra.setcategoria(rs.getString("cat_nome"));
        gra.setVendas(rs.getInt("vendas"));
        gra.setMesAno(rs.getString("mes_ano"));

        return gra;
    }

//    public Resultado<Grafico> buscarMesesIntervalo(EntidadeDominio entidade) {
//        try {
//            if(connection == null || connection.isClosed()) {
//                connection = Conexao.getConnectionMySQL();
//            }
//
//            Grafico grafico = (Grafico) entidade;
//            List<String> meses = new ArrayList<>();
//
//            if(grafico.getDataInicio() != null && grafico.getDataFim() != null) {
//                StringBuilder sql = new StringBuilder(
//                        "SELECT DISTINCT DATE_FORMAT(e.est_dt_cadastro, '%Y-%m') AS mes_ano " +
//                        "FROM crud_v3.estoque e " +
//                        "WHERE e.est_dt_cadastro >= ? AND e.est_dt_cadastro <= ? " +
//                        "ORDER BY mes_ano" );
//
//                try(PreparedStatement pst = connection.prepareStatement(sql.toString())) {
//                    pst.setObject(1, grafico.getDataInicio());
//                    pst.setObject(2, grafico.getDataFim());
//
//                    try(ResultSet rs = pst.executeQuery()) {
//                        while(rs.next()) {
//                            meses.add(rs.getString("mes_ano"));
//                        }
//                    }
//                }
//            }
//            Grafico gra = new Grafico();
//            gra.setLabels(meses);
//            return Resultado.sucesso(gra);
//
//        } catch (Exception e) {
//            System.err.println("Erro ao buscar meses do intervalo: " + e.getMessage());
//            return Resultado.erro("Erro ao buscar meses do intervalo: " + e.getMessage());
//        } finally {
//            try {
//                if (connection != null) connection.close();
//            } catch (SQLException closeEx) {
//                System.err.println("Erro ao fechar recursos: " + closeEx.getMessage());
//            }
//        }
//    }
}
