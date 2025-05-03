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

        if(troca.getCliente() == null || troca.getCliente().getId() == null){
            sb.append("Cliente é obrigatório e deve ter um ID válido.\n");
        }

        if(troca.getValorTotal() <= 0){
            sb.append("O valor do Pedido deve ser maior que zero.\n");
        }

        return null;
    }
}
