package Dominio;

import Enums.Status;
import java.util.List;

public class TrocaSolicitada extends EntidadeDominio {
    private Pedido pedido;
    private Status status;
    private Cliente cliente;
    private Double valorTotal;
    private List<Status> listStatus;

    public TrocaSolicitada() {
    }

    public TrocaSolicitada(Pedido pedido,Status status){
        this.pedido = pedido;
        this.status = status;
    }

    public TrocaSolicitada(Integer id, Pedido pedido, Status status){
        super (id);
        this.pedido = pedido;
        this.status = status;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<Status> getListStatus() {
        return listStatus;
    }

    public void setListStatus(List<Status> listStatus) {
        this.listStatus = listStatus;
    }
}
