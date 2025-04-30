package Strategy;

import Dao.PedidoDAO;
import Dominio.EntidadeDominio;
import Dominio.TrocaSolicitada;
import Dominio.Pedido;
import Enums.Status;
import Util.Resultado;

import java.util.List;

public class ValidaStatusTrocaPedido implements IStrategy{
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        TrocaSolicitada troca = (TrocaSolicitada) entidade;
        Pedido pedido = new Pedido();
        pedido.setId(troca.getPedido().getId());
        PedidoDAO PedidoDAO = new PedidoDAO();

        Resultado<List<EntidadeDominio>> resultadoPedido = PedidoDAO.consultar(pedido);
        List<EntidadeDominio> pedidos = resultadoPedido.getValor();

        if(pedidos.isEmpty()){
            sb.append("Não existe um pedido com o id: ").append(pedido.getId());
        }

        Pedido pedidoConsultado = (Pedido) pedidos.get(0);
        Status statusPedido = pedidoConsultado.getStatus();

        if(statusPedido != Status.ENTREGUE){
            sb.append("O pedido deve estar em status ENTREGUE para troca ser solicitada.");
        }
        return null;
    }
}
