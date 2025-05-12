package Strategy;

import Dao.DevolucaoDAO;
import Dao.TrocaSolicitadaDAO;
import Dominio.Devolucao;
import Dominio.EntidadeDominio;
import Dominio.Pedido;
import Dominio.TrocaSolicitada;
import Util.Resultado;

import java.util.List;

public class VerificaExistenciaDevolucao implements IStrategy{
    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        TrocaSolicitadaDAO trocaSolicitadaDAO = new TrocaSolicitadaDAO();
        TrocaSolicitada troca = (TrocaSolicitada) entidade;
        Resultado<List<EntidadeDominio>> resultadoConsulta = trocaSolicitadaDAO.consultar(troca);
        if(!resultadoConsulta.isSucesso()){
            sb.append(resultadoConsulta.getErro());
            return Resultado.erro(null);
        }
        List<EntidadeDominio> trocas = resultadoConsulta.getValor();

        troca = (TrocaSolicitada) trocas.getFirst();

        Pedido pedido = troca.getPedido();
        Devolucao devolucao = new Devolucao();
        devolucao.setPedido(pedido);
        DevolucaoDAO devolucaoDAO = new DevolucaoDAO();

        Resultado<List<EntidadeDominio>> resultadoDevolucao = devolucaoDAO.consultar(devolucao);
        if(!resultadoDevolucao.isSucesso()){
            sb.append(resultadoDevolucao.getErro());
            return Resultado.erro(null);
        }
        List<EntidadeDominio> devolucoes = resultadoDevolucao.getValor();

        if(!devolucoes.isEmpty()){
            sb.append("Já existe uma devolução para essa troca. Não é possível cancelar essa troca.");
        }
        return Resultado.sucesso(null);
    }
}
