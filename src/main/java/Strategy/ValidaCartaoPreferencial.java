package Strategy;

import Dao.CartaoDAO;
import Dominio.Cartao;
import Dominio.EntidadeDominio;
import Util.Resultado;

import java.util.List;

public class ValidaCartaoPreferencial implements IStrategy{
    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        Cartao cartao = (Cartao) entidade;
        if(cartao.getPreferencial()){
            CartaoDAO cartaoDAO = new CartaoDAO();
            Cartao cartaoPreferencial = new Cartao();
            cartaoPreferencial.setPreferencial(true);
            cartaoPreferencial.setCliente(cartao.getCliente());
            Resultado<List<EntidadeDominio>> resultadoCartoes = cartaoDAO.consultar(cartaoPreferencial);
            List<EntidadeDominio> cartoes = resultadoCartoes.getValor();

            if(!cartoes.isEmpty()){
                sb.append("Ja existe um cartao preferencial cadastrado.");
            }
        }
        return null;
    }
}
