package Strategy;

import Dominio.EntidadeDominio;
import Dominio.DevolucaoProduto;
import Util.Resultado;

public class ValidaDadosDevolucaoProduto implements IStrategy {

    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        if (!(entidade instanceof DevolucaoProduto devolucaoProduto)) {
            sb.append("Entidade não é uma devolução de produto válida.");
            return Resultado.erro(sb.toString());
        }

        if (devolucaoProduto.getDevolucao() == null) {
            sb.append("Devolução não informada.");
            return Resultado.erro(sb.toString());
        }

        if (devolucaoProduto.getProduto() == null) {
            sb.append("Produto não informado.");
            return Resultado.erro(sb.toString());
        }

        if (devolucaoProduto.getQuantidade() == null) {
            sb.append("Quantidade não informada.");
            return Resultado.erro(sb.toString());
        }

        if (devolucaoProduto.getQuantidade() <= 0) {
            sb.append("Quantidade deve ser positiva.");
            return Resultado.erro(sb.toString());
        }

        return null;
    }
}