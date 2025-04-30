package Strategy;

import Dao.BandeiraDAO;
import Dominio.Bandeira;
import Dominio.Cartao;
import Dominio.EntidadeDominio;
import Util.Resultado;

import java.util.List;

public class ValidaBandeiraExistente implements IStrategy{
    @Override
    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb) {
        Cartao cartao = (Cartao) entidade;
        Bandeira bandeira = cartao.getBandeira();
        BandeiraDAO bandeiraDAO = new BandeiraDAO();
        Resultado<List<EntidadeDominio>> resultadoCartoes = bandeiraDAO.consultar(bandeira);
        List<EntidadeDominio> cartoes = resultadoCartoes.getValor();
        if(cartoes.isEmpty()){
            sb.append("A bandeira deve estar cadastrada no sitema.");
        }
        return null;
    }
}
