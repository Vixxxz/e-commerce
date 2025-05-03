package Dominio;

import Enums.Ativo;
import Enums.TipoCupom;

public class Cupom extends EntidadeDominio {
    private String codigo;
    private Double valor;
    private TipoCupom tipo;
    private Cliente cliente;
    private Pedido pedido;
    private Ativo status;

    public Cupom() {
    }

    public Cupom(String codigo, Double valor, TipoCupom tipo, Cliente cliente, Pedido pedido) {
        this.codigo = codigo;
        this.valor = valor;
        this.tipo = tipo;
        this.cliente = cliente;
        this.pedido = pedido;
    }

    public Cupom(String codigo, Double valor, TipoCupom tipo) {
        this.codigo = codigo;
        this.valor = valor;
        this.tipo = tipo;
    }

    public Cupom(String codigo, Double valor, TipoCupom tipo, Pedido pedido) {
        this.codigo = codigo;
        this.valor = valor;
        this.tipo = tipo;
        this.pedido = pedido;
    }

    public Cupom(String codigo, Double valor, TipoCupom tipo, Cliente cliente) {
        this.codigo = codigo;
        this.valor = valor;
        this.tipo = tipo;
        this.cliente = cliente;
    }

    public Cupom(Integer id, String codigo, Double valor, TipoCupom tipo, Cliente cliente, Pedido pedido) {
        super(id);
        this.codigo = codigo;
        this.valor = valor;
        this.tipo = tipo;
        this.cliente = cliente;
        this.pedido = pedido;
    }

    public Cupom(Integer id, String codigo, Double valor, TipoCupom tipo) {
        super(id);
        this.codigo = codigo;
        this.valor = valor;
        this.tipo = tipo;
    }

    public Cupom(Integer id, String codigo, Double valor, TipoCupom tipo, Pedido pedido) {
        super(id);
        this.codigo = codigo;
        this.valor = valor;
        this.tipo = tipo;
        this.pedido = pedido;
    }

    public Cupom(Integer id, String codigo, Double valor, TipoCupom tipo, Cliente cliente) {
        super(id);
        this.codigo = codigo;
        this.valor = valor;
        this.tipo = tipo;
        this.cliente = cliente;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public TipoCupom getTipo() {
        return tipo;
    }

    public void setTipo(TipoCupom tipo) {
        this.tipo = tipo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Ativo getStatus() {
        return status;
    }

    public void setStatus(Ativo status) {
        this.status = status;
    }
}
