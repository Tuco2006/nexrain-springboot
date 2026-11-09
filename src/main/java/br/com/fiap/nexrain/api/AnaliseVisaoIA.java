package br.com.fiap.nexrain.api;

// resultado da leitura da imagem da camera (pela OpenAI ou pelo motor local)
public class AnaliseVisaoIA {

    private boolean alagamentoDetectado;
    private int nivelAguaEstimadoCm;
    private String nivelRisco;
    private double confianca;
    private boolean viaTransitavel;
    private String descricao;
    private String origem;

    public AnaliseVisaoIA() {
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public boolean isAlagamentoDetectado() {
        return alagamentoDetectado;
    }

    public void setAlagamentoDetectado(boolean alagamentoDetectado) {
        this.alagamentoDetectado = alagamentoDetectado;
    }

    public int getNivelAguaEstimadoCm() {
        return nivelAguaEstimadoCm;
    }

    public void setNivelAguaEstimadoCm(int nivelAguaEstimadoCm) {
        this.nivelAguaEstimadoCm = nivelAguaEstimadoCm;
    }

    public String getNivelRisco() {
        return nivelRisco;
    }

    public void setNivelRisco(String nivelRisco) {
        this.nivelRisco = nivelRisco;
    }

    public double getConfianca() {
        return confianca;
    }

    public void setConfianca(double confianca) {
        this.confianca = confianca;
    }

    public boolean isViaTransitavel() {
        return viaTransitavel;
    }

    public void setViaTransitavel(boolean viaTransitavel) {
        this.viaTransitavel = viaTransitavel;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
