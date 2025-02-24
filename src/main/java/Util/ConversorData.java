package Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConversorData {
    private static final SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

    public static Resultado<Date> converter(String dataStr) {
        try {
            Date data = formato.parse(dataStr);
            return Resultado.sucesso(data);
        } catch (ParseException e) {
            return Resultado.erro("Data inválida: " + dataStr);
        }
    }
}

