package Util;

public class Resultado<T> {
    private final T valor;
    private final String erro;

    private Resultado(T valor, String erro) {
        this.valor = valor;
        this.erro = erro;
    }

    public static <T> Resultado<T> sucesso(T valor) {
        return new Resultado<>(valor, null);
    }

    public static <T> Resultado<T> erro(String erro) {
        return new Resultado<>(null, erro);
    }

    public boolean isSucesso() {
        return erro == null;
    }

    public T getValor() {
        return valor;
    }

    public String getErro() {
        return erro;
    }
}

