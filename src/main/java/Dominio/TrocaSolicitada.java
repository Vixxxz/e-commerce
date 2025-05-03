package Dominio;

import Enums.Status;
import java.util.List;

public class TrocaSolicitada extends EntidadeDominio {
    private Pedido pedido;
    private Integer quantidade;
    private Status status;
    private List<Status> listStatus;
    private Cliente cliente;

    public TrocaSolicitada() {
    }

    public TrocaSolicitada(Pedido pedido, Integer quantidade ,Status status){
        this.pedido = pedido;
        this.quantidade = quantidade;
        this.status = status;
    }

    public TrocaSolicitada(Integer id, Pedido pedido, Integer quantidade, Status status){
        super (id);
        this.pedido = pedido;
        this.quantidade = quantidade;
        this.status = status;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Status> getListStatus() {
        return listStatus;
    }

    public void setListStatus(List<Status> listStatus) {
        this.listStatus = listStatus;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}
