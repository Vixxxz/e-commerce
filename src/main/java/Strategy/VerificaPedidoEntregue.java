package Strategy;

import Dao.PedidoDAO;
import Dominio.EntidadeDominio;
import Dominio.Pedido;
import Enums.Status;
import Util.Resultado;

import java.util.List;

public class VerificaPedidoEntregue implements IStrategy{
    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        Pedido pedido = (Pedido) entidade;
        PedidoDAO pedidoDAO = new PedidoDAO();
        Resultado<List<EntidadeDominio>> resultadoPedido = pedidoDAO.consultar(pedido);
        if(!resultadoPedido.isSucesso()){
            sb.append(resultadoPedido.getErro());
        }
        List<EntidadeDominio> pedidos = resultadoPedido.getValor();
        pedido = (Pedido) pedidos.getFirst();
        if(pedido.getStatus() == Status.ENTREGUE){
            sb.append("O pedido já foi entregue e não pode ser cancelado");
            return Resultado.erro(null);
        }
        return Resultado.sucesso(null);
    }
}
