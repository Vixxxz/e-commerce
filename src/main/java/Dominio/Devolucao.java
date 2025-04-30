package Dominio;

public class Devolucao extends EntidadeDominio{
    private Pedido pedido;
    private Double valor;

    public Devolucao(Pedido pedido, Double valor) {
        this.pedido = pedido;
        this.valor = valor;
    }

    public Devolucao(Integer id, Pedido pedido, Double valor) {
        super(id);
        this.pedido = pedido;
        this.valor = valor;
    }

    public Devolucao(){}

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
}
