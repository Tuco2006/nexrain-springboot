package br.com.fiap.nexrain.dto;

// o que o front manda pra IA analisar: uma camera cadastrada ou uma foto enviada pelo cidadao
public class EntradaAnaliseImagem {

    private Integer idCamera;
    private String imagemBase64;
    private String urlImagem;
    private Integer idRegiao;
    private String endereco;
    private Double latitude;
    private Double longitude;
    private boolean registrarOcorrencia = true;

    public EntradaAnaliseImagem() {
    }

    public Integer getIdCamera() {
        return idCamera;
    }

    public void setIdCamera(Integer idCamera) {
        this.idCamera = idCamera;
    }

    public String getImagemBase64() {
        return imagemBase64;
    }

    public void setImagemBase64(String imagemBase64) {
        this.imagemBase64 = imagemBase64;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public Integer getIdRegiao() {
        return idRegiao;
    }

    public void setIdRegiao(Integer idRegiao) {
        this.idRegiao = idRegiao;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isRegistrarOcorrencia() {
        return registrarOcorrencia;
    }

    public void setRegistrarOcorrencia(boolean registrarOcorrencia) {
        this.registrarOcorrencia = registrarOcorrencia;
    }
}
