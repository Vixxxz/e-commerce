package Strategy;

import Dao.TrocaSolicitadaDAO;
import Dominio.EntidadeDominio;
import Dominio.Pedido;
import Dominio.TrocaSolicitada;
import Util.Resultado;

import java.util.List;

public class VerificaExistenciaTroca implements IStrategy{
    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        Pedido pedido = (Pedido) entidade;
        TrocaSolicitada troca = new TrocaSolicitada();
        troca.setPedido(pedido);
        TrocaSolicitadaDAO trocaSolicitadaDAO = new TrocaSolicitadaDAO();
        Resultado<List<EntidadeDominio>> resultadoConsulta = trocaSolicitadaDAO.consultar(troca);
        if(!resultadoConsulta.isSucesso()){
            sb.append(resultadoConsulta.getErro());
            return Resultado.erro(null);
        }
        List<EntidadeDominio> trocas = resultadoConsulta.getValor();
        if(!trocas.isEmpty()){
            sb.append("Já existe uma troca para esse pedido. Não é possível cancelar esse pedido.");
        }
        return Resultado.sucesso(null);
    }
}
