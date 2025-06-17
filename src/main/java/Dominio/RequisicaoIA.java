package Dominio;

public class RequisicaoIA extends EntidadeDominio{
    private Cliente cliente;
    private String pergunta;
    private String resposta;
    private String historicoConversa;

    public RequisicaoIA() {}

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getPergunta() {
        return pergunta;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public String getHistoricoConversa() {
        return historicoConversa;
    }

    public void setHistoricoConversa(String historicoConversa) {
        this.historicoConversa = historicoConversa;
    }
}
