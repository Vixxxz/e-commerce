package Strategy;

import Dao.MarcaDAO;
import Dominio.EntidadeDominio;
import Dominio.Produto;
import Util.Resultado;

import java.util.List;

public class VerificaAlteracaoMarca implements IStrategy{
    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        MarcaDAO marcaDAO = new MarcaDAO();
        Produto produto = (Produto) entidade;
        Resultado<List<EntidadeDominio>> resultadoMarca = marcaDAO.consultar(produto.getMarca());
        List<EntidadeDominio>marcas = resultadoMarca.getValor();

        if(marcas.isEmpty()) {
            sb.append("Marca não cadastrada no sistema.");
        }
        return null;
    }
}
