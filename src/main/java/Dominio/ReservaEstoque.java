package Dominio;

public class ReservaEstoque extends EntidadeDominio{
    private Produto produto;
    private Marca marca;
    private Integer quantidade;
    private String sessao;

    public ReservaEstoque(Produto produto, Marca marca, Integer quantidade, String sessao) {
        this.produto = produto;
        this.marca = marca;
        this.quantidade = quantidade;
        this.sessao = sessao;
    }

    public ReservaEstoque(Integer id, Produto produto, Marca marca, Integer quantidade, String sessao) {
        super(id);
        this.produto = produto;
        this.marca = marca;
        this.quantidade = quantidade;
        this.sessao = sessao;
    }

    public ReservaEstoque(){}

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getSessao() {
        return sessao;
    }

    public void setSessao(String sessao) {
        this.sessao = sessao;
    }
}
