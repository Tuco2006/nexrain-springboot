package br.com.fiap.nexrain.dto;

import br.com.fiap.nexrain.api.AnaliseVisaoIA;
import br.com.fiap.nexrain.entities.Camera;
import br.com.fiap.nexrain.entities.Ocorrencia;

public class ResultadoAnaliseImagem {

    private AnaliseVisaoIA analise;
    private String origemAnalise;
    private Camera camera;
    private Ocorrencia ocorrencia;
    private String mensagem;

    public ResultadoAnaliseImagem() {
    }

    public ResultadoAnaliseImagem(AnaliseVisaoIA analise, String origemAnalise, Camera camera, Ocorrencia ocorrencia, String mensagem) {
        this.analise = analise;
        this.origemAnalise = origemAnalise;
        this.camera = camera;
        this.ocorrencia = ocorrencia;
        this.mensagem = mensagem;
    }

    public AnaliseVisaoIA getAnalise() {
        return analise;
    }

    public void setAnalise(AnaliseVisaoIA analise) {
        this.analise = analise;
    }

    public String getOrigemAnalise() {
        return origemAnalise;
    }

    public void setOrigemAnalise(String origemAnalise) {
        this.origemAnalise = origemAnalise;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(Ocorrencia ocorrencia) {
        this.ocorrencia = ocorrencia;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
