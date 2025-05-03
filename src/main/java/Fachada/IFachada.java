package Fachada;

import Dominio.EntidadeDominio;
import Util.Resultado;

import java.util.List;

public interface IFachada {
    Resultado<String> salvar(EntidadeDominio entidade);
    Resultado<String> alterar(EntidadeDominio entidade);
    Resultado<String> excluir (EntidadeDominio entidade);
    Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade);
}
