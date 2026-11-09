package br.com.fiap.nexrain.entities;

import br.com.fiap.nexrain.excecoes.RegraNegocioException;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Ocorrencia {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private int id;
    private int idRegiao;
    private Integer idCamera;
    private String endereco;
    private double latitude;
    private double longitude;
    private LocalDateTime dataHora;
    private int nivelAguaCm;
    private String nivelRisco;
    private double confianca;
    private String origem;
    private String status;
    private String descricao;
    private String validadoPor;
    private LocalDateTime dataValidacao;

    public Ocorrencia() {
        super();
    }

    public Ocorrencia(int id, int idRegiao, Integer idCamera, String endereco, double latitude, double longitude,
                      LocalDateTime dataHora, int nivelAguaCm, String nivelRisco, double confianca, String origem,
                      String status, String descricao, String validadoPor, LocalDateTime dataValidacao) {
        super();
        this.id = id;
        this.idRegiao = idRegiao;
        this.idCamera = idCamera;
        this.endereco = endereco;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dataHora = dataHora;
        this.nivelAguaCm = nivelAguaCm;
        this.nivelRisco = nivelRisco;
        this.confianca = confianca;
        this.origem = origem;
        this.status = status;
        this.descricao = descricao;
        this.validadoPor = validadoPor;
        this.dataValidacao = dataValidacao;
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

    public Integer getIdCamera() {
        return idCamera;
    }

    public void setIdCamera(Integer idCamera) {
        this.idCamera = idCamera;
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

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public int getNivelAguaCm() {
        return nivelAguaCm;
    }

    public void setNivelAguaCm(int nivelAguaCm) {
        this.nivelAguaCm = nivelAguaCm;
    }

    public String getNivelRisco() {
        return nivelRisco;
    }

    public void setNivelRisco(String nivelRisco) {
        this.nivelRisco = nivelRisco;
    }

    public double getConfianca() {
        return confianca;
    }

    public void setConfianca(double confianca) {
        this.confianca = confianca;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getValidadoPor() {
        return validadoPor;
    }

    public void setValidadoPor(String validadoPor) {
        this.validadoPor = validadoPor;
    }

    public LocalDateTime getDataValidacao() {
        return dataValidacao;
    }

    public void setDataValidacao(LocalDateTime dataValidacao) {
        this.dataValidacao = dataValidacao;
    }

    public Coordenada coordenada() {
        return new Coordenada(latitude, longitude);
    }

    public boolean ativa() {
        return "PENDENTE".equals(status) || "CONFIRMADA".equals(status);
    }

    public int raioImpactoMetros() {
        switch (nivelRisco == null ? "" : nivelRisco) {
            case "CRITICO":
                return 300;
            case "ALTO":
                return 200;
            case "MEDIO":
                return 120;
            default:
                return 60;
        }
    }

    public double penalidadeRota() {
        double peso;
        switch (nivelRisco == null ? "" : nivelRisco) {
            case "CRITICO":
                peso = 120;
                break;
            case "ALTO":
                peso = 45;
                break;
            case "MEDIO":
                peso = 12;
                break;
            default:
                peso = 3;
        }
        return "CONFIRMADA".equals(status) ? peso : peso * 0.7;
    }

    public boolean exigeAlerta() {
        return "ALTO".equals(nivelRisco) || "CRITICO".equals(nivelRisco);
    }

    public void confirmar(String responsavel) {
        if (!"PENDENTE".equals(status)) {
            throw new RegraNegocioException("Só é possível confirmar uma ocorrência pendente (status atual: " + status + ")");
        }
        registrarValidacao("CONFIRMADA", responsavel);
    }

    public void descartar(String responsavel) {
        if (!"PENDENTE".equals(status)) {
            throw new RegraNegocioException("Só é possível descartar uma ocorrência pendente (status atual: " + status + ")");
        }
        registrarValidacao("DESCARTADA", responsavel);
    }

    public void resolver(String responsavel) {
        if (!ativa()) {
            throw new RegraNegocioException("A ocorrência já está " + status.toLowerCase());
        }
        registrarValidacao("RESOLVIDA", responsavel);
    }

    private void registrarValidacao(String novoStatus, String responsavel) {
        if (responsavel == null || responsavel.isBlank()) {
            throw new RegraNegocioException("Informe quem está validando a ocorrência");
        }
        this.status = novoStatus;
        this.validadoPor = responsavel.trim();
        this.dataValidacao = LocalDateTime.now(FUSO).withNano(0);
    }

    @Override
    public String toString() {
        return "Ocorrencia " +
                "\nid=" + id +
                "\nidRegiao=" + idRegiao +
                "\nendereco='" + endereco + '\'' +
                "\nnivelAguaCm=" + nivelAguaCm +
                "\nnivelRisco='" + nivelRisco + '\'' +
                "\norigem='" + origem + '\'' +
                "\nstatus='" + status + '\'';
    }
}
