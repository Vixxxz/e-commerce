package Fachada;

import Dominio.EntidadeDominio;
import Util.Resultado;

import java.util.List;

public interface IFachada {
    String salvar(List<EntidadeDominio> entidades) throws Exception;
    Resultado<String> alterar(EntidadeDominio entidade);
    Resultado<String> excluir(EntidadeDominio entidade);
    Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade);
}
