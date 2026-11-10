package br.com.fiap.nexrain.entities;

import java.util.regex.Pattern;

public class Camera {

    private int id;
    private int idRegiao;
    private String nome;
    private String endereco;
    private double latitude;
    private double longitude;
    private String urlImagem;
    private String status;

    public Camera() {
        super();
    }

    public Camera(int id, int idRegiao, String nome, String endereco, double latitude, double longitude, String urlImagem, String status) {
        super();
        this.id = id;
        this.idRegiao = idRegiao;
        this.nome = nome;
        this.endereco = endereco;
        this.latitude = latitude;
        this.longitude = longitude;
        this.urlImagem = urlImagem;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdRegiao() {
        return idRegiao;
    }

    public void setIdRegiao(int idRegiao) {
        this.idRegiao = idRegiao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
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

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean ativa() {
        return "ATIVA".equals(status);
    }

    public boolean isAoVivo() {
        return urlImagem != null && urlImagem.matches("https://cameras\\.cetsp\\.com\\.br/cams/\\d+/\\d+\\.jpg");
    }

    public String getUrlAoVivo() {
        if (!isAoVivo()) {
            return null;
        }
        return urlImagem.substring(0, urlImagem.lastIndexOf('/') + 1);
    }

    public boolean aceitaImagem(String url) {
        if (url == null) {
            return false;
        }
        if (url.equals(urlImagem)) {
            return true;
        }
        return isAoVivo() && url.matches(Pattern.quote(getUrlAoVivo()) + "\\d+\\.jpg(\\?\\d+)?");
    }

    @Override
    public String toString() {
        return "Camera " +
                "\nid=" + id +
                "\nidRegiao=" + idRegiao +
                "\nnome='" + nome + '\'' +
                "\nendereco='" + endereco + '\'' +
                "\nstatus='" + status + '\'';
    }
}
