package br.com.fiap.nexrain.api;

import java.util.ArrayList;
import java.util.List;

// condicao atual e previsao de chuva de um ponto, vinda da API Open-Meteo
public class PrevisaoClima {

    private double latitude;
    private double longitude;
    private String horaReferencia;
    private double temperatura;
    private int umidade;
    private double precipitacaoAtual;
    private int codigoTempo;
    private String descricaoTempo;
    private boolean simulado;
    private String fonte = "Open-Meteo";
    private List<PrevisaoHora> proximasHoras = new ArrayList<PrevisaoHora>();

    public PrevisaoClima() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getHoraReferencia() {
        return horaReferencia;
    }

    public void setHoraReferencia(String horaReferencia) {
        this.horaReferencia = horaReferencia;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

    public int getUmidade() {
        return umidade;
    }

    public void setUmidade(int umidade) {
        this.umidade = umidade;
    }

    public double getPrecipitacaoAtual() {
        return precipitacaoAtual;
    }

    public void setPrecipitacaoAtual(double precipitacaoAtual) {
        this.precipitacaoAtual = precipitacaoAtual;
    }

    public int getCodigoTempo() {
        return codigoTempo;
    }

    public void setCodigoTempo(int codigoTempo) {
        this.codigoTempo = codigoTempo;
        this.descricaoTempo = descreverCodigo(codigoTempo);
    }

    public String getDescricaoTempo() {
        return descricaoTempo;
    }

    public boolean isSimulado() {
        return simulado;
    }

    public String getFonte() {
        return fonte;
    }

    public void setFonte(String fonte) {
        this.fonte = fonte;
    }

    public List<PrevisaoHora> getProximasHoras() {
        return proximasHoras;
    }

    public void setProximasHoras(List<PrevisaoHora> proximasHoras) {
        this.proximasHoras = proximasHoras;
    }

    // soma da chuva prevista nas proximas N horas
    public double chuvaAcumulada(int horas) {
        double total = 0;
        for (int i = 0; i < Math.min(horas, proximasHoras.size()); i++) {
            total += proximasHoras.get(i).getPrecipitacaoMm();
        }
        return Math.round(total * 10) / 10.0;
    }

    // hora mais forte de chuva prevista nas proximas N horas
    public double chuvaMaximaHora(int horas) {
        double maximo = precipitacaoAtual;
        for (int i = 0; i < Math.min(horas, proximasHoras.size()); i++) {
            maximo = Math.max(maximo, proximasHoras.get(i).getPrecipitacaoMm());
        }
        return maximo;
    }

    public int probabilidadeMaxima(int horas) {
        int maximo = 0;
        for (int i = 0; i < Math.min(horas, proximasHoras.size()); i++) {
            maximo = Math.max(maximo, proximasHoras.get(i).getProbabilidade());
        }
        return maximo;
    }

    // cenario de temporal de verao, usado na demonstracao quando nao esta chovendo de verdade
    public PrevisaoClima simularTemporal(double intensidade) {
        PrevisaoClima simulada = new PrevisaoClima();
        simulada.latitude = latitude;
        simulada.longitude = longitude;
        simulada.horaReferencia = horaReferencia;
        simulada.temperatura = temperatura;
        simulada.umidade = 95;
        simulada.setCodigoTempo(95);
        simulada.precipitacaoAtual = Math.round(intensidade * 0.6 * 10) / 10.0;
        simulada.simulado = true;
        simulada.fonte = fonte + " + cenário simulado";

        // pico de chuva na segunda hora e depois diminui
        double[] curva = {0.6, 1.0, 0.8, 0.5, 0.3, 0.2};
        for (int i = 0; i < proximasHoras.size(); i++) {
            PrevisaoHora original = proximasHoras.get(i);
            double fator = i < curva.length ? curva[i] : 0.1;
            double chuva = Math.max(original.getPrecipitacaoMm(), Math.round(intensidade * fator * 10) / 10.0);
            int probabilidade = Math.max(original.getProbabilidade(), i < curva.length ? 95 - i * 5 : 40);
            simulada.proximasHoras.add(new PrevisaoHora(original.getHora(), chuva, probabilidade));
        }
        return simulada;
    }

    // codigos WMO usados pelo Open-Meteo
    public static String descreverCodigo(int codigo) {
        if (codigo == 0) return "Céu limpo";
        if (codigo <= 2) return "Parcialmente nublado";
        if (codigo == 3) return "Nublado";
        if (codigo == 45 || codigo == 48) return "Neblina";
        if (codigo >= 51 && codigo <= 57) return "Garoa";
        if (codigo >= 61 && codigo <= 65) return codigo == 65 ? "Chuva forte" : "Chuva";
        if (codigo == 66 || codigo == 67) return "Chuva congelante";
        if (codigo >= 71 && codigo <= 77) return "Neve";
        if (codigo >= 80 && codigo <= 82) return codigo == 82 ? "Pancadas de chuva violentas" : "Pancadas de chuva";
        if (codigo >= 95) return "Tempestade";
        return "Indefinido";
    }
}
