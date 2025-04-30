package Util;

import java.security.SecureRandom;

public class CupomGenerator {
    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String gerarCodigoCupom(int tamanho) {
        StringBuilder sb = new StringBuilder(tamanho);

        for (int i = 0; i < tamanho; i++) {
            int indice = random.nextInt(CARACTERES.length());
            sb.append(CARACTERES.charAt(indice));
        }

        return sb.toString();
    }
}
