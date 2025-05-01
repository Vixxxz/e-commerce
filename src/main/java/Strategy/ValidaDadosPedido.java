package Strategy;

import Dominio.EntidadeDominio;
import Dominio.Pedido;
import Util.Resultado;

public class ValidaDadosPedido implements IStrategy {

    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        if (!(entidade instanceof Pedido pedido)) {
            sb.append("Entidade não é um pedido válido.");
            return Resultado.erro(sb.toString());
        }

        if (pedido.getValorTotal() == null) {
            sb.append("Valor total do pedido não informado.");
            return Resultado.erro(sb.toString());
        }

        if (pedido.getValorTotal() <= 0) {
            sb.append("Valor total do pedido deve ser positivo.");
            return Resultado.erro(sb.toString());
        }

        if (pedido.getStatus() == null) {
            sb.append("Status do pedido não informado.");
            return Resultado.erro(sb.toString());
        }

        if (pedido.getTransportadora() == null) {
            sb.append("Transportadora não informada.");
            return Resultado.erro(sb.toString());
        }

        if (pedido.getClienteEndereco() == null) {
            sb.append("Endereço do cliente não informado.");
            return Resultado.erro(sb.toString());
        }

        return null;
    }
}