package Strategy;

import Dao.ReservaDAO;
import Dominio.EntidadeDominio;
import Dominio.ReservaEstoque;
import Enums.Ativo;
import Util.Resultado;

import java.util.List;

public class VerificaReservaAtiva implements IStrategy{
    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        ReservaDAO reservaDAO = new ReservaDAO();
        ReservaEstoque reserva = (ReservaEstoque) entidade;
        reserva.setStatus(Ativo.ATIVO);

        Resultado<List<EntidadeDominio>> resultadoReserva = reservaDAO.consultar(reserva);
        if(!resultadoReserva.isSucesso()){
            sb.append(resultadoReserva.getErro());
            return null;
        }
        List<EntidadeDominio> reservas = resultadoReserva.getValor();

        if(reservas.isEmpty()){
            sb.append("Nenhuma reserva ativa encontrada para esse pedido, por favor realizar uma nova reserva.");
        }

        return null;
    }
}
