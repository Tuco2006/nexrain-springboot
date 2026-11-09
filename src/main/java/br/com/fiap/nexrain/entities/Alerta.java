package br.com.fiap.nexrain.entities;

import java.time.LocalDateTime;

public class Alerta {

    private int id;
    private int idRegiao;
    private Integer idOcorrencia;
    private String titulo;
    private String mensagem;
    private String nivel;
    private String origem;
    private LocalDateTime dataEmissao;
    private LocalDateTime dataExpiracao;
    private boolean ativo = true;

    public Alerta() {
        super();
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

    public Integer getIdOcorrencia() {
        return idOcorrencia;
    }

    public void setIdOcorrencia(Integer idOcorrencia) {
        this.idOcorrencia = idOcorrencia;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean vigente(LocalDateTime agora) {
        return ativo && (dataExpiracao == null || dataExpiracao.isAfter(agora));
    }

    @Override
    public String toString() {
        return "Alerta " +
                "\nid=" + id +
                "\nidRegiao=" + idRegiao +
                "\ntitulo='" + titulo + '\'' +
                "\nnivel='" + nivel + '\'' +
                "\nativo=" + ativo;
    }
}
