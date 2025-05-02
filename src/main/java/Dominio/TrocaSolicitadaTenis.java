package Dominio;

public class TrocaSolicitadaTenis extends EntidadeDominio{
    private TrocaSolicitada troca;
    private Produto produto;
    private Integer quantidade;

    public TrocaSolicitadaTenis(){}

    public TrocaSolicitadaTenis(TrocaSolicitada troca, Produto produto, Integer quantidade){
        this.troca = troca;
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public TrocaSolicitadaTenis(Integer id, TrocaSolicitada troca, Produto produto, Integer quantidade){
        super (id);
        this.troca = troca;
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public TrocaSolicitada getTroca() {
        return troca;
    }

    public void setTroca(TrocaSolicitada troca) {
        this.troca = troca;
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
