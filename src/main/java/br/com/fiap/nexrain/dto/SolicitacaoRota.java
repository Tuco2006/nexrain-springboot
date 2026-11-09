package br.com.fiap.nexrain.dto;

public class SolicitacaoRota {

    private String origem;
    private Double origemLat;
    private Double origemLon;
    private String destino;
    private Double destinoLat;
    private Double destinoLon;

    public SolicitacaoRota() {
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public Double getOrigemLat() {
        return origemLat;
    }

    public void setOrigemLat(Double origemLat) {
        this.origemLat = origemLat;
    }

    public Double getOrigemLon() {
        return origemLon;
    }

    public void setOrigemLon(Double origemLon) {
        this.origemLon = origemLon;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public Double getDestinoLat() {
        return destinoLat;
    }

    public void setDestinoLat(Double destinoLat) {
        this.destinoLat = destinoLat;
    }

    public Double getDestinoLon() {
        return destinoLon;
    }

    public void setDestinoLon(Double destinoLon) {
        this.destinoLon = destinoLon;
    }
}
