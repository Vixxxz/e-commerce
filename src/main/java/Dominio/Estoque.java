package Dominio;

public class Estoque extends EntidadeDominio {
    private Integer quantidade;
    private Produto produto;
    private Double valorCusto;
    private Marca marca;
    private Integer movimentacao;

    public Estoque(){}

    public Estoque(Integer quantidade, Produto produto, Double valorCusto, Marca marca, Integer movimentacao) {
        this.quantidade = quantidade;
        this.produto = produto;
        this.valorCusto = valorCusto;
        this.marca = marca;
        this.movimentacao = movimentacao;
    }

    public Estoque(Integer id, Integer quantidade, Produto produto, Double valorCusto, Marca marca, Integer movimentacao) {
        super(id);
        this.quantidade = quantidade;
        this.produto = produto;
        this.valorCusto = valorCusto;
        this.marca = marca;
        this.movimentacao = movimentacao;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Double getValorCusto() {
        return valorCusto;
    }

    public void setValorCusto(Double valorCusto) {
        this.valorCusto = valorCusto;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Integer getMovimentacao() {
        return movimentacao;
    }

    public void setMovimentacao(Integer movimentacao) {
        this.movimentacao = movimentacao;
    }
}
