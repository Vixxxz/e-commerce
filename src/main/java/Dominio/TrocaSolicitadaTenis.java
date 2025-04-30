package Dominio;

public class TrocaSolicitadaTenis extends EntidadeDominio{
    private TrocaSolicitada troca;
    private Produto produto;

    public TrocaSolicitadaTenis(){}

    public TrocaSolicitadaTenis(TrocaSolicitada troca, Produto produto){
        this.troca = troca;
        this.produto = produto;
    }

    public TrocaSolicitadaTenis(Integer id, TrocaSolicitada troca, Produto produto){
        super (id);
        this.troca = troca;
        this.produto = produto;
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
}
