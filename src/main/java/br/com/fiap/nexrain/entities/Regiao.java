package br.com.fiap.nexrain.entities;

public class Regiao {

    private int id;
    private String nome;
    private String cidade;
    private double latitude;
    private double longitude;
    private String suscetibilidade;
    private double limiteChuvaMm;

    public Regiao() {
        super();
    }

    public Regiao(int id, String nome, String cidade, double latitude, double longitude, String suscetibilidade, double limiteChuvaMm) {
        super();
        this.id = id;
        this.nome = nome;
        this.cidade = cidade;
        this.latitude = latitude;
        this.longitude = longitude;
        this.suscetibilidade = suscetibilidade;
        this.limiteChuvaMm = limiteChuvaMm;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
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

    public String getSuscetibilidade() {
        return suscetibilidade;
    }

    public void setSuscetibilidade(String suscetibilidade) {
        this.suscetibilidade = suscetibilidade;
    }

    public double getLimiteChuvaMm() {
        return limiteChuvaMm;
    }

    public void setLimiteChuvaMm(double limiteChuvaMm) {
        this.limiteChuvaMm = limiteChuvaMm;
    }

    public Coordenada coordenada() {
        return new Coordenada(latitude, longitude);
    }

    public int pesoSuscetibilidade() {
        if ("ALTA".equals(suscetibilidade)) {
            return 20;
        }
        if ("MEDIA".equals(suscetibilidade)) {
            return 10;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Regiao " +
                "\nid=" + id +
                "\nnome='" + nome + '\'' +
                "\ncidade='" + cidade + '\'' +
                "\nsuscetibilidade='" + suscetibilidade + '\'' +
                "\nlimiteChuvaMm=" + limiteChuvaMm;
    }
}
