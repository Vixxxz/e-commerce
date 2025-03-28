package Dominio;

public class Marca extends EntidadeDominio{
    private String nome;

    public Marca(){}

    public Marca(String nome) {
        this.nome = nome;
    }

    public Marca(Integer id, String nome) {
        super(id);
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
