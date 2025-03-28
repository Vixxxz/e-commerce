package Strategy;

import Dao.CategoriaDAO;
import Dominio.EntidadeDominio;
import Dominio.Produto;
import Util.Resultado;

import java.util.List;

public class VerificaAlteracaoCategoria implements IStrategy{

    @Override
    public String processar(EntidadeDominio entidade, StringBuilder sb) {
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        Produto produto = (Produto) entidade;

        Resultado<List<EntidadeDominio>>resultadoCategoria = categoriaDAO.consultar(produto.getCategoria());
        List<EntidadeDominio>categorias = resultadoCategoria.getValor();

        if(categorias.isEmpty()){
            sb.append("Categoria não cadastrada no sistema.");
        }
        return null;
    }
}
