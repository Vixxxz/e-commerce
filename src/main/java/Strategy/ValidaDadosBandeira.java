package Strategy;

import Dominio.Bandeira;
import Dominio.EntidadeDominio;
import Util.Resultado;

public class ValidaDadosBandeira implements IStrategy{
    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        Bandeira bandeira = (Bandeira) entidade;

        if (bandeira.getNomeBandeira() == null || bandeira.getNomeBandeira().isBlank()) {
            sb.append("Nome da bandeira é obrigatório.");
            return Resultado.sucesso(sb.toString());
        }
        return null;
    }
}
