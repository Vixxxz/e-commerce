package Dominio;

public class DevolucaoProduto extends EntidadeDominio{
    private Devolucao devolucao;
    private Produto produto;
    private Integer quantidade;

    public DevolucaoProduto(Devolucao devolucao, Produto produto, Integer quantidade) {
        this.devolucao = devolucao;
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public DevolucaoProduto(Integer id, Devolucao devolucao, Produto produto, Integer quantidade) {
        super(id);
        this.devolucao = devolucao;
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public DevolucaoProduto(){}

    public Devolucao getDevolucao() {
        return devolucao;
    }

    public void setDevolucao(Devolucao devolucao) {
        this.devolucao = devolucao;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
