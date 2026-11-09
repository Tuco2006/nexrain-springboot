package br.com.fiap.nexrain.dto;

import br.com.fiap.nexrain.api.PrevisaoClima;
import br.com.fiap.nexrain.entities.MonitorRisco;
import br.com.fiap.nexrain.entities.Regiao;

import java.util.List;

public class RiscoRegiao {

    private Regiao regiao;
    private int indice;
    private String nivel;
    private List<String> fatores;
    private String recomendacao;
    private int ocorrenciasAtivas;
    private double precipitacaoAtual;
    private double chuvaMaxima6h;
    private double chuvaAcumulada3h;
    private int probabilidadeChuva;
    private double temperatura;
    private String descricaoTempo;
    private boolean simulado;

    public RiscoRegiao() {
    }

    public RiscoRegiao(MonitorRisco monitor) {
        PrevisaoClima previsao = monitor.getPrevisao();

        this.regiao = monitor.getRegiao();
        this.indice = monitor.calcularIndiceRisco();
        this.nivel = monitor.classificarNivel();
        this.fatores = monitor.listarFatores();
        this.recomendacao = monitor.gerarRecomendacao();
        this.ocorrenciasAtivas = monitor.getOcorrenciasAtivas().size();

        if (previsao != null) {
            this.precipitacaoAtual = previsao.getPrecipitacaoAtual();
            this.chuvaMaxima6h = previsao.chuvaMaximaHora(6);
            this.chuvaAcumulada3h = previsao.chuvaAcumulada(3);
            this.probabilidadeChuva = previsao.probabilidadeMaxima(6);
            this.temperatura = previsao.getTemperatura();
            this.descricaoTempo = previsao.getDescricaoTempo();
            this.simulado = previsao.isSimulado();
        }
    }

    public Regiao getRegiao() {
        return regiao;
    }

    public int getIndice() {
        return indice;
    }

    public String getNivel() {
        return nivel;
    }

    public List<String> getFatores() {
        return fatores;
    }

    public String getRecomendacao() {
        return recomendacao;
    }

    public int getOcorrenciasAtivas() {
        return ocorrenciasAtivas;
    }

    public double getPrecipitacaoAtual() {
        return precipitacaoAtual;
    }

    public double getChuvaMaxima6h() {
        return chuvaMaxima6h;
    }

    public double getChuvaAcumulada3h() {
        return chuvaAcumulada3h;
    }

    public int getProbabilidadeChuva() {
        return probabilidadeChuva;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public String getDescricaoTempo() {
        return descricaoTempo;
    }

    public boolean isSimulado() {
        return simulado;
    }
}
