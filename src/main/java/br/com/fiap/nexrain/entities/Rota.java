package br.com.fiap.nexrain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Rota {

    private int id;
    private String origem;
    private double origemLat;
    private double origemLon;
    private String destino;
    private double destinoLat;
    private double destinoLon;
    private double distanciaKm;
    private double duracaoMin;
    private double distanciaDiretaKm;
    private double duracaoDiretaMin;
    private int alagamentosEvitados;
    private int alagamentosNoTrajeto;
    private String situacao;
    private List<Coordenada> trajeto = new ArrayList<Coordenada>();
    private List<Coordenada> trajetoDireto = new ArrayList<Coordenada>();
    private LocalDateTime dataCalculo;

    public Rota() {
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public double getOrigemLat() {
        return origemLat;
    }

    public void setOrigemLat(double origemLat) {
        this.origemLat = origemLat;
    }

    public double getOrigemLon() {
        return origemLon;
    }

    public void setOrigemLon(double origemLon) {
        this.origemLon = origemLon;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public double getDestinoLat() {
        return destinoLat;
    }

    public void setDestinoLat(double destinoLat) {
        this.destinoLat = destinoLat;
    }

    public double getDestinoLon() {
        return destinoLon;
    }

    public void setDestinoLon(double destinoLon) {
        this.destinoLon = destinoLon;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public double getDuracaoMin() {
        return duracaoMin;
    }

    public void setDuracaoMin(double duracaoMin) {
        this.duracaoMin = duracaoMin;
    }

    public double getDistanciaDiretaKm() {
        return distanciaDiretaKm;
    }

    public void setDistanciaDiretaKm(double distanciaDiretaKm) {
        this.distanciaDiretaKm = distanciaDiretaKm;
    }

    public double getDuracaoDiretaMin() {
        return duracaoDiretaMin;
    }

    public void setDuracaoDiretaMin(double duracaoDiretaMin) {
        this.duracaoDiretaMin = duracaoDiretaMin;
    }

    public int getAlagamentosEvitados() {
        return alagamentosEvitados;
    }

    public void setAlagamentosEvitados(int alagamentosEvitados) {
        this.alagamentosEvitados = alagamentosEvitados;
    }

    public int getAlagamentosNoTrajeto() {
        return alagamentosNoTrajeto;
    }

    public void setAlagamentosNoTrajeto(int alagamentosNoTrajeto) {
        this.alagamentosNoTrajeto = alagamentosNoTrajeto;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public List<Coordenada> getTrajeto() {
        return trajeto;
    }

    public void setTrajeto(List<Coordenada> trajeto) {
        this.trajeto = trajeto;
    }

    public List<Coordenada> getTrajetoDireto() {
        return trajetoDireto;
    }

    public void setTrajetoDireto(List<Coordenada> trajetoDireto) {
        this.trajetoDireto = trajetoDireto;
    }

    public LocalDateTime getDataCalculo() {
        return dataCalculo;
    }

    public void setDataCalculo(LocalDateTime dataCalculo) {
        this.dataCalculo = dataCalculo;
    }

    public Coordenada coordenadaOrigem() {
        return new Coordenada(origemLat, origemLon);
    }

    public Coordenada coordenadaDestino() {
        return new Coordenada(destinoLat, destinoLon);
    }

    // quanto tempo a mais o motorista gasta pra fugir dos alagamentos
    public double getMinutosExtras() {
        return Math.max(0, Math.round((duracaoMin - duracaoDiretaMin) * 10) / 10.0);
    }

    @Override
    public String toString() {
        return "Rota " +
                "\nid=" + id +
                "\norigem='" + origem + '\'' +
                "\ndestino='" + destino + '\'' +
                "\ndistanciaKm=" + distanciaKm +
                "\nduracaoMin=" + duracaoMin +
                "\nalagamentosEvitados=" + alagamentosEvitados +
                "\nsituacao='" + situacao + '\'';
    }
}
