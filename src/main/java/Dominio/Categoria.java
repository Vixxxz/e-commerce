package Dominio;

public class Categoria extends EntidadeDominio{
    private String nome;
    private Double percentual;

    public Categoria(){}

    public Categoria(String nome, Double percentual) {
        this.nome = nome;
        this.percentual = percentual;
    }

    public Categoria(Integer id, String nome, Double percentual) {
        super(id);
        this.nome = nome;
        this.percentual = percentual;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getPercentual() {
        return percentual;
    }

    public void setPercentual(Double percentual) {
        this.percentual = percentual;
    }
}
