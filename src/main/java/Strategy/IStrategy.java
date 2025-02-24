package Strategy;

import Dominio.EntidadeDominio;

public interface IStrategy {
    String processar(EntidadeDominio entidade, StringBuilder sb);
}
