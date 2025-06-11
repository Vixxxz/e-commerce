package Dominio;

import java.util.Date;
import java.util.List;

public class Grafico extends EntidadeDominio{
    private String categoria;
    private Integer vendas;
    private String mesAno;
    private List<String> labels;
    private Date dataInicio;
    private Date dataFim;

    public Grafico() {
    }

    public String getcategoria() {
        return categoria;
    }

    public void setcategoria(String label) {
        this.categoria = label;
    }

    public Integer getVendas() {
        return vendas;
    }

    public void setVendas(Integer data) {
        this.vendas = data;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public String getMesAno() {
        return mesAno;
    }

    public void setMesAno(String mesAno) {
        this.mesAno = mesAno;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
