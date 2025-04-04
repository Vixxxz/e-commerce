package Strategy;

import Dao.ProdutoDAO;
import Dominio.EntidadeDominio;
import Dominio.Produto;
import Util.Resultado;

import java.util.List;

public class VerificaExistenciaProduto implements IStrategy{
    @Override
    public String processar(EntidadeDominio entidade, StringBuilder sb) {
        Produto pro = new Produto();
        pro.setId(entidade.getId());
        ProdutoDAO produtoDAO = new ProdutoDAO();
        Resultado<List<EntidadeDominio>>resultadoProdutos = produtoDAO.consultar(pro);
        List<EntidadeDominio>produtos = resultadoProdutos.getValor();
        if(produtos.isEmpty()){
            sb.append("Produto com o id: ").append(pro.getId()).append(" não encontrado");
        }
        return null;
    }
}
