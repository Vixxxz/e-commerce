package Strategy;

import Dao.EstoqueDAO;
import Dao.ReservaDAO;
import Dominio.EntidadeDominio;
import Dominio.Estoque;
import Dominio.ReservaEstoque;
import Util.Resultado;

import java.util.List;

public class ValidaEstoque implements IStrategy{
    @Override
    public String processar(EntidadeDominio entidade, StringBuilder sb) {
        ReservaEstoque reservaEstoque = (ReservaEstoque) entidade;
        ReservaDAO reservaDAO = new ReservaDAO();
        ReservaEstoque filtro = new ReservaEstoque();
        filtro.setProduto(reservaEstoque.getProduto());

        Resultado<List<EntidadeDominio>> resultadoReserva = reservaDAO.consultar(filtro);
        if(!resultadoReserva.isSucesso()){
            sb.append(resultadoReserva.getErro());
            return null;
        }
        List<EntidadeDominio> reservas = resultadoReserva.getValor();

        int quantidadeReservada = reservas.stream()
                .map(re -> (ReservaEstoque) re)
                .mapToInt(ReservaEstoque::getQuantidade)
                .sum();

        EstoqueDAO estoqueDAO = new EstoqueDAO();
        Estoque estoque = new Estoque();
        estoque.setProduto(reservaEstoque.getProduto());

        Resultado<List<EntidadeDominio>> resultadoEstoque = estoqueDAO.consultar(estoque);
        if(!resultadoEstoque.isSucesso()){
            sb.append(resultadoEstoque.getErro());
            return null;
        }
        List<EntidadeDominio> estoques = resultadoEstoque.getValor();

        estoque = (Estoque) estoques.getFirst();

        if((estoque.getQuantidade() - (quantidadeReservada + reservaEstoque.getQuantidade())) < 0){
            sb.append("Quantidade no estoque insuficiente");
        }
        return null;
    }
}
