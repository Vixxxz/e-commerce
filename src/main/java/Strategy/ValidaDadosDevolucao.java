package Strategy;

import Dominio.EntidadeDominio;
import Dominio.Devolucao;
import Dominio.Pedido;
import Util.Resultado;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ValidaDadosDevolucao implements IStrategy {

    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        if (!(entidade instanceof Devolucao devolucao)) {
            sb.append("Entidade não é uma devolução válida.");
            return Resultado.erro(null);
        }

        if (devolucao.getPedido() == null) {
            sb.append("Pedido não informado para devolução.\n");
            return Resultado.erro(null);
        }

        if (devolucao.getValor() == null) {
            sb.append("Valor da devolução não informado.\n");
            return Resultado.erro(null);
        } else if (devolucao.getValor() <= 0) {
            sb.append("Valor da devolução deve ser positivo.\n");
            return Resultado.erro(null);
        }

        return null;
    }
}