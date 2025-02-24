package Strategy;

import Dominio.Cliente;
import Dominio.EntidadeDominio;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncriptografaSenha implements IStrategy{

    public String processar(EntidadeDominio entidade, StringBuilder sb){
        Cliente cliente = (Cliente) entidade;
        String senha = cliente.getSenha();
        return encripta(senha, sb);
    }

    private String encripta(String senha, StringBuilder sb) {
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256"); //messageDisgest ira usar o algoritmo SHA-256
            byte[] hash = messageDigest.digest(senha.getBytes());   //transforma a senha em um array de bytes e digest calcula o hash
            return Base64.getEncoder().encodeToString(hash);    //converte o array de bytes hash em uma String usando codificação Base64
        } catch (NoSuchAlgorithmException e) {
            sb.append("Erro na encriptação da senha. ");
            return senha;
        }
    }
}
