package br.com.fiap.nexrain.dto;

import br.com.fiap.nexrain.entities.Ocorrencia;
import br.com.fiap.nexrain.entities.Rota;

import java.util.List;

public class ResultadoRota {

    private Rota rota;
    private List<Ocorrencia> alagamentosNoCaminhoDireto;
    private List<Ocorrencia> alagamentosNoTrajeto;
    private int alternativasAvaliadas;
    private String mensagem;

    public ResultadoRota() {
    }

    public ResultadoRota(Rota rota, List<Ocorrencia> alagamentosNoCaminhoDireto, List<Ocorrencia> alagamentosNoTrajeto,
                         int alternativasAvaliadas, String mensagem) {
        this.rota = rota;
        this.alagamentosNoCaminhoDireto = alagamentosNoCaminhoDireto;
        this.alagamentosNoTrajeto = alagamentosNoTrajeto;
        this.alternativasAvaliadas = alternativasAvaliadas;
        this.mensagem = mensagem;
    }

    public Rota getRota() {
        return rota;
    }

    public List<Ocorrencia> getAlagamentosNoCaminhoDireto() {
        return alagamentosNoCaminhoDireto;
    }

    public List<Ocorrencia> getAlagamentosNoTrajeto() {
        return alagamentosNoTrajeto;
    }

    public int getAlternativasAvaliadas() {
        return alternativasAvaliadas;
    }

    public String getMensagem() {
        return mensagem;
    }
}
