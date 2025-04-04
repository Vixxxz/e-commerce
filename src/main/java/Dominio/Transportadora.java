package Dominio;

public class Transportadora extends EntidadeDominio{
    private String nome;
    private Double valor;

    public Transportadora(){}

    public Transportadora(String nome, Double valor) {
        this.nome = nome;
        this.valor = valor;
    }

    public Transportadora(Integer id, String nome, Double valor) {
        super(id);
        this.nome = nome;
        this.valor = valor;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
}
