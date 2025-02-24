package Dominio;

public class Endereco extends EntidadeDominio{
    private String cep;
    private String logradouro;
    private String tipoLogradouro;
    private Bairro bairro;

    public Endereco() {
    }

    public Endereco(Integer id, String cep, Bairro bairro, String logradouro, String tipoLogradouro) {
        this.id = id;
        this.cep = cep;
        this.bairro = bairro;
        this.logradouro = logradouro;
        this.tipoLogradouro = tipoLogradouro;
    }

    public Endereco(String cep, Bairro bairro, String logradouro, String tipoLogradouro) {
        this.cep = cep;
        this.bairro = bairro;
        this.logradouro = logradouro;
        this.tipoLogradouro = tipoLogradouro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public Bairro getBairro() {
        return bairro;
    }

    public void setBairro(Bairro bairro) {
        this.bairro = bairro;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getTipoLogradouro() {
        return tipoLogradouro;
    }

    public void setTipoLogradouro(String tipoLogradouro) {
        this.tipoLogradouro = tipoLogradouro;
    }

    @Override
    public String toString() {
        return "Endereco{" +
                "cep='" + cep + '\'' +
                ", logradouro='" + logradouro + '\'' +
                ", tipoLogradouro='" + tipoLogradouro + '\'' +
                ", bairro=" + bairro +
                ", id=" + id +
                "} " + super.toString();
    }
}
