package br.com.fiap.nexrain.api;

// uma hora da previsao do Open-Meteo
public class PrevisaoHora {

    private String hora;
    private double precipitacaoMm;
    private int probabilidade;

    public PrevisaoHora() {
    }

    public PrevisaoHora(String hora, double precipitacaoMm, int probabilidade) {
        this.hora = hora;
        this.precipitacaoMm = precipitacaoMm;
        this.probabilidade = probabilidade;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public double getPrecipitacaoMm() {
        return precipitacaoMm;
    }

    public void setPrecipitacaoMm(double precipitacaoMm) {
        this.precipitacaoMm = precipitacaoMm;
    }

    public int getProbabilidade() {
        return probabilidade;
    }

    public void setProbabilidade(int probabilidade) {
        this.probabilidade = probabilidade;
    }
}
