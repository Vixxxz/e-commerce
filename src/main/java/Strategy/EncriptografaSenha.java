package Strategy;

import Dominio.Cliente;
import Dominio.EntidadeDominio;
import Util.Resultado;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncriptografaSenha implements IStrategy{

    public Resultado<String> processar(EntidadeDominio entidade, StringBuilder sb){
        Cliente cliente = (Cliente) entidade;
        String senha = cliente.getSenha();
        return Resultado.sucesso(encripta(senha, sb));
    }

    private String encripta(String senha, StringBuilder sb) {
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(senha.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            sb.append("Erro na encriptação da senha. ");
            return senha;
        }
    }
}
