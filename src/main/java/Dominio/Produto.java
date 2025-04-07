package Dominio;

import Enums.Genero;

public class Produto extends EntidadeDominio{
    private String sku;
    private String nome;
    private Double preco;
    private String modelo;
    private String cor;
    private Integer tamanho;
    private Genero genero;
    private String descricao;
    private Marca marca;
    private Categoria categoria;
    private Boolean ativo;
    private String caminhoFoto;

    public Produto() {}

    //sem preço
    public Produto(String sku, String nome,
                   String modelo, String cor, Integer tamanho,
                   Genero genero, String descricao, Marca marca,
                   Categoria categoria, Boolean ativo, String caminhoFoto) {
        this.sku = sku;
        this.nome = nome;
        this.modelo = modelo;
        this.cor = cor;
        this.tamanho = tamanho;
        this.genero = genero;
        this.descricao = descricao;
        this.marca = marca;
        this.categoria = categoria;
        this.ativo = ativo;
        this.caminhoFoto = caminhoFoto;
    }

    //sem preço e sem descrição
    public Produto(String sku, String nome,
                   String modelo, String cor, Integer tamanho,
                   Genero genero, Marca marca,
                   Categoria categoria, Boolean ativo, String caminhoFoto) {
        this.sku = sku;
        this.nome = nome;
        this.modelo = modelo;
        this.cor = cor;
        this.tamanho = tamanho;
        this.genero = genero;
        this.marca = marca;
        this.categoria = categoria;
        this.ativo = ativo;
        this.caminhoFoto = caminhoFoto;
    }

    //tudo menos id
    public Produto(String sku, String nome, Double preco,
                   String modelo, String cor, Integer tamanho,
                   Genero genero, String descricao, Marca marca,
                   Categoria categoria, Boolean ativo, String caminhoFoto) {
        this.sku = sku;
        this.nome = nome;
        this.preco = preco;
        this.modelo = modelo;
        this.cor = cor;
        this.tamanho = tamanho;
        this.genero = genero;
        this.descricao = descricao;
        this.marca = marca;
        this.categoria = categoria;
        this.ativo = ativo;
        this.caminhoFoto = caminhoFoto;
    }

    //sem descrição
    public Produto(String sku, String nome, Double preco,
                   String modelo, String cor, Integer tamanho,
                   Genero genero, Marca marca, Categoria categoria,
                   Boolean ativo, String caminhoFoto) {
        this.sku = sku;
        this.nome = nome;
        this.preco = preco;
        this.modelo = modelo;
        this.cor = cor;
        this.tamanho = tamanho;
        this.genero = genero;
        this.marca = marca;
        this.categoria = categoria;
        this.ativo = ativo;
        this.caminhoFoto = caminhoFoto;
    }

    public Produto(Integer id, String sku, String nome,
                   Double preco, String modelo, String cor,
                   Integer tamanho, Genero genero, String descricao,
                   Marca marca, Categoria categoria, Boolean ativo, String caminhoFoto) {
        super(id);
        this.sku = sku;
        this.nome = nome;
        this.preco = preco;
        this.modelo = modelo;
        this.cor = cor;
        this.tamanho = tamanho;
        this.genero = genero;
        this.descricao = descricao;
        this.marca = marca;
        this.categoria = categoria;
        this.ativo = ativo;
        this.caminhoFoto = caminhoFoto;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public Integer getTamanho() {
        return tamanho;
    }

    public void setTamanho(Integer tamanho) {
        this.tamanho = tamanho;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }

    @Override
    public String toString() {
        return "Produto{" +
                "sku='" + sku + '\'' +
                ", nome='" + nome + '\'' +
                ", preco=" + preco +
                ", modelo='" + modelo + '\'' +
                ", cor='" + cor + '\'' +
                ", tamanho=" + tamanho +
                ", genero='" + genero + '\'' +
                ", descricao='" + descricao + '\'' +
                ", marca=" + marca +
                ", categoria=" + categoria +
                ", id=" + id +
                "} ";
    }
}
