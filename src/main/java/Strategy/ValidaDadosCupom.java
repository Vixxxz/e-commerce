package Strategy;

import Dominio.EntidadeDominio;
import Dominio.Cupom;
import Util.Resultado;

public class ValidaDadosCupom implements IStrategy {

    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        if (!(entidade instanceof Cupom cupom)) {
            sb.append("Entidade não é um cupom válido.");
            return Resultado.erro(sb.toString());
        }

        if (cupom.getCodigo() == null || cupom.getCodigo().isBlank()) {
            sb.append("Código do cupom é obrigatório.");
            return Resultado.erro(sb.toString());
        }

        if (cupom.getValor() == null) {
            sb.append("Valor do cupom não informado.");
            return Resultado.erro(sb.toString());
        }

        if (cupom.getValor() <= 0) {
            sb.append("Valor do cupom deve ser positivo.");
            return Resultado.erro(sb.toString());
        }

        if (cupom.getTipo() == null) {
            sb.append("Tipo do cupom não informado.");
            return Resultado.erro(sb.toString());
        }

        return null;
    }
}