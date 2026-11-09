package br.com.fiap.nexrain.dto;

// resultado da busca de endereco (Nominatim / OpenStreetMap)
public class LocalEncontrado {

    private String nome;
    private double latitude;
    private double longitude;

    public LocalEncontrado() {
    }

    public LocalEncontrado(String nome, double latitude, double longitude) {
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getNome() {
        return nome;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
