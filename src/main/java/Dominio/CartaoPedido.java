package Dominio;

public class CartaoPedido extends EntidadeDominio{
    private Pedido pedido;
    private Cartao cartao;

    public CartaoPedido() {
    }

    public CartaoPedido(Pedido pedido, Cartao cartao) {
        this.pedido = pedido;
        this.cartao = cartao;
    }

    public CartaoPedido(Integer id, Pedido pedido, Cartao cartao) {
        super(id);
        this.pedido = pedido;
        this.cartao = cartao;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Cartao getCartao() {
        return cartao;
    }

    public void setCartao(Cartao cartao) {
        this.cartao = cartao;
    }
}
