package Dao;

import Dominio.Cliente;
import Dominio.EntidadeDominio;
import Util.Resultado;

import java.sql.SQLException;
import java.util.List;

public interface IDAO {
    EntidadeDominio salvar(EntidadeDominio entidade) throws SQLException, ClassNotFoundException;
    Resultado<EntidadeDominio> alterar(EntidadeDominio entidade);
    Resultado<String> excluir(EntidadeDominio entidade);
    Resultado<List<EntidadeDominio>> consultar(EntidadeDominio entidade);
}
