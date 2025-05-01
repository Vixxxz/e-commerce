package Strategy;

import Dominio.EntidadeDominio;
import Dominio.CartaoPedido;
import Util.Resultado;

public class ValidaDadosCartaoPedido implements IStrategy {

    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        if (!(entidade instanceof CartaoPedido cartaoPedido)) {
            sb.append("Entidade não é um vínculo válido entre cartão e pedido.");
            return Resultado.erro(sb.toString());
        }

        if (cartaoPedido.getPedido() == null) {
            sb.append("Pedido não informado.");
            return Resultado.erro(sb.toString());
        }

        if (cartaoPedido.getCartao() == null) {
            sb.append("Cartão não informado.");
            return Resultado.erro(sb.toString());
        }

        return null;
    }
}