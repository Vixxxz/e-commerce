package Strategy;

import Dao.PedidoDAO;
import Dao.TrocaSolicitadaDAO;
import Dominio.Devolucao;
import Dominio.EntidadeDominio;
import Dominio.Pedido;
import Dominio.TrocaSolicitada;
import Enums.Status;
import Util.Resultado;

import java.util.List;

public class VerificaStatusPedidoParaDevolucao implements IStrategy{

    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        Devolucao devolucao = (Devolucao) entidade;

        PedidoDAO pedidoDAO = new PedidoDAO();
        Resultado<List<EntidadeDominio>> resultadoConsultaPedidos = pedidoDAO.consultar(devolucao.getPedido());
        List<EntidadeDominio>pedidos = resultadoConsultaPedidos.getValor();
        Pedido pedido = (Pedido) pedidos.getFirst();

        if(pedido.getStatus() != Status.TROCA_AUTORIZADA){
            sb.append("Troca ainda não está autorizada");
            return Resultado.erro(null);
        }

        TrocaSolicitadaDAO trocaSolicitadaDAO = new TrocaSolicitadaDAO();
        TrocaSolicitada trocaSolicitada = new TrocaSolicitada();
        trocaSolicitada.setPedido(pedido);
        Resultado<List<EntidadeDominio>> resultadoConsultaTrocas = trocaSolicitadaDAO.consultar(trocaSolicitada);
        List<EntidadeDominio>trocas = resultadoConsultaTrocas.getValor();
        trocaSolicitada = (TrocaSolicitada) trocas.getFirst();

        if(trocaSolicitada.getStatus() != Status.TROCA_AUTORIZADA){
            sb.append("Troca ainda não está autorizada");
            return Resultado.erro(null);
        }
        return Resultado.sucesso(null);
    }
}
