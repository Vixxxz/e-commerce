package Strategy;

import Dominio.EntidadeDominio;
import Util.Resultado;

public interface IStrategy {
    Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb);
}
