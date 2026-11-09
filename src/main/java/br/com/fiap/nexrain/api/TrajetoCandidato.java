package br.com.fiap.nexrain.api;

import br.com.fiap.nexrain.entities.Coordenada;

import java.util.ArrayList;
import java.util.List;

// um caminho possivel devolvido pelo servico de rotas (OSRM)
public class TrajetoCandidato {

    private String descricao;
    private double distanciaMetros;
    private double duracaoSegundos;
    private List<Coordenada> pontos = new ArrayList<Coordenada>();

    public TrajetoCandidato() {
    }

    public TrajetoCandidato(String descricao, double distanciaMetros, double duracaoSegundos, List<Coordenada> pontos) {
        this.descricao = descricao;
        this.distanciaMetros = distanciaMetros;
        this.duracaoSegundos = duracaoSegundos;
        this.pontos = pontos;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getDistanciaMetros() {
        return distanciaMetros;
    }

    public void setDistanciaMetros(double distanciaMetros) {
        this.distanciaMetros = distanciaMetros;
    }

    public double getDuracaoSegundos() {
        return duracaoSegundos;
    }

    public void setDuracaoSegundos(double duracaoSegundos) {
        this.duracaoSegundos = duracaoSegundos;
    }

    public List<Coordenada> getPontos() {
        return pontos;
    }

    public void setPontos(List<Coordenada> pontos) {
        this.pontos = pontos;
    }

    public double getDuracaoMinutos() {
        return duracaoSegundos / 60.0;
    }
}
