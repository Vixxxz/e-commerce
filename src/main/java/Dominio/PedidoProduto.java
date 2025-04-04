package Dominio;

public class PedidoProduto extends EntidadeDominio{
    private Pedido pedido;
    private Produto produto;
    private Integer quantidade;

    public PedidoProduto() {}

    public PedidoProduto(Pedido pedido, Produto produto, Integer quantidade) {
        this.pedido = pedido;
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public PedidoProduto(Integer id, Pedido pedido, Produto produto, Integer quantidade) {
        super(id);
        this.pedido = pedido;
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
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
