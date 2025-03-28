package Strategy;

import Dao.ProdutoDAO;
import Dominio.EntidadeDominio;
import Dominio.Produto;
import Util.Resultado;

import java.util.List;

public class VerificaDuplicataProduto implements IStrategy{
    @Override
    public String processar(EntidadeDominio entidade, StringBuilder sb) {
        Produto pro = (Produto)entidade;
        ProdutoDAO produtoDAO = new ProdutoDAO();
        Resultado<List<EntidadeDominio>> resultadoProdutos = produtoDAO.consultar(pro);
        List<EntidadeDominio>produtos = resultadoProdutos.getValor();
        if(!produtos.isEmpty()){
            sb.append("Já existe um produto cadastrado.");
        }
        return null;
    }
}
