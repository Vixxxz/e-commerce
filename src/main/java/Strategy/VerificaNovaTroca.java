package Strategy;

import Dominio.EntidadeDominio;
import Dominio.Pedido;
import Enums.Status;
import Util.Resultado;

public class VerificaNovaTroca implements IStrategy{

    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        Pedido pedido = (Pedido) entidade;
        if(pedido.getStatus() == Status.TROCA_AUTORIZADA){
            return Resultado.sucesso(null);
        }
        return Resultado.erro(null);
    }
}
