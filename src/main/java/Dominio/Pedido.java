package Dominio;

import Enums.Status;

public class Pedido extends EntidadeDominio{
    private Double valorTotal;
    private Status status;
    private Transportadora transportadora;
    private ClienteEndereco clienteEndereco;

    public Pedido() {}

    public Pedido(Double valorTotal, Status status, Transportadora transportadora, ClienteEndereco clienteEndereco) {
        this.valorTotal = valorTotal;
        this.status = status;
        this.transportadora = transportadora;
        this.clienteEndereco = clienteEndereco;
    }

    public Pedido(Integer id, Double valorTotal, Status status, Transportadora transportadora, ClienteEndereco clienteEndereco) {
        super(id);
        this.valorTotal = valorTotal;
        this.status = status;
        this.transportadora = transportadora;
        this.clienteEndereco = clienteEndereco;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Transportadora getTransportadora() {
        return transportadora;
    }

    public void setTransportadora(Transportadora transportadora) {
        this.transportadora = transportadora;
    }

    public ClienteEndereco getClienteEndereco() {
        return clienteEndereco;
    }

    public void setClienteEndereco(ClienteEndereco clienteEndereco) {
        this.clienteEndereco = clienteEndereco;
    }
}
