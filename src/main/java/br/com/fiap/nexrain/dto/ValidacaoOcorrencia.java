package br.com.fiap.nexrain.dto;

// acao do operador (Defesa Civil / CGE) sobre uma ocorrencia
public class ValidacaoOcorrencia {

    private String acao;
    private String responsavel;

    public ValidacaoOcorrencia() {
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }
}
