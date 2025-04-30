package Strategy;

import Dominio.EntidadeDominio;
import Dominio.TrocaSolicitada;
import Util.Resultado;

public class ValidaDadosTrocaSolicitada implements IStrategy{
    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb){
        TrocaSolicitada troca = (TrocaSolicitada) entidade;

        if(troca.getPedido() == null || troca.getPedido().getId() == null){
            sb.append("Pedido é obrigatório e deve ter um ID válido.\n");
        }

        if(troca.getQuantidade() == null || troca.getQuantidade() <= 0){
            sb.append("A quantidade deve conter no mínimo um número maior que zero.\n");
        }

        return null;
    }
}
