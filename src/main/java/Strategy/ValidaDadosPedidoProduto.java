package Strategy;

import Dominio.EntidadeDominio;
import Dominio.PedidoProduto;
import Util.Resultado;

public class ValidaDadosPedidoProduto implements IStrategy {

    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        if (!(entidade instanceof PedidoProduto pedidoProduto)) {
            sb.append("Entidade não é um item de pedido válido.");
            return Resultado.erro(sb.toString());
        }

        if (pedidoProduto.getPedido() == null) {
            sb.append("Pedido não informado.");
            return Resultado.erro(sb.toString());
        }

        if (pedidoProduto.getProduto() == null) {
            sb.append("Produto não informado.");
            return Resultado.erro(sb.toString());
        }

        if (pedidoProduto.getQuantidade() == null) {
            sb.append("Quantidade não informada.");
            return Resultado.erro(sb.toString());
        }

        if (pedidoProduto.getQuantidade() <= 0) {
            sb.append("Quantidade deve ser maior que zero.");
            return Resultado.erro(sb.toString());
        }

        return null;
    }
}