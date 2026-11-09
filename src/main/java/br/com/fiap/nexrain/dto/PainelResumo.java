package br.com.fiap.nexrain.dto;

// numeros mostrados na pagina inicial
public class PainelResumo {

    private int regioesMonitoradas;
    private int camerasAtivas;
    private int ocorrenciasAtivas;
    private int ocorrenciasPendentes;
    private int alertasAtivos;
    private int rotasCalculadas;

    public PainelResumo() {
    }

    public PainelResumo(int regioesMonitoradas, int camerasAtivas, int ocorrenciasAtivas, int ocorrenciasPendentes,
                        int alertasAtivos, int rotasCalculadas) {
        this.regioesMonitoradas = regioesMonitoradas;
        this.camerasAtivas = camerasAtivas;
        this.ocorrenciasAtivas = ocorrenciasAtivas;
        this.ocorrenciasPendentes = ocorrenciasPendentes;
        this.alertasAtivos = alertasAtivos;
        this.rotasCalculadas = rotasCalculadas;
    }

    public int getRegioesMonitoradas() {
        return regioesMonitoradas;
    }

    public int getCamerasAtivas() {
        return camerasAtivas;
    }

    public int getOcorrenciasAtivas() {
        return ocorrenciasAtivas;
    }

    public int getOcorrenciasPendentes() {
        return ocorrenciasPendentes;
    }

    public int getAlertasAtivos() {
        return alertasAtivos;
    }

    public int getRotasCalculadas() {
        return rotasCalculadas;
    }
}
