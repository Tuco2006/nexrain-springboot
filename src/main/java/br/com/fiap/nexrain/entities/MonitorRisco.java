package br.com.fiap.nexrain.entities;

import br.com.fiap.nexrain.api.PrevisaoClima;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MonitorRisco {

    private static final String[] NIVEIS = {"BAIXO", "MEDIO", "ALTO", "CRITICO"};

    private Regiao regiao;
    private PrevisaoClima previsao;
    private List<Ocorrencia> ocorrenciasAtivas;

    public MonitorRisco() {
    }

    public MonitorRisco(Regiao regiao, PrevisaoClima previsao, List<Ocorrencia> ocorrencias) {
        this.regiao = regiao;
        this.previsao = previsao;
        this.ocorrenciasAtivas = new ArrayList<Ocorrencia>();

        if (ocorrencias != null) {
            for (Ocorrencia ocorrencia : ocorrencias) {
                if (ocorrencia.getIdRegiao() == regiao.getId() && ocorrencia.ativa()) {
                    this.ocorrenciasAtivas.add(ocorrencia);
                }
            }
        }
    }

    public Regiao getRegiao() {
        return regiao;
    }

    public PrevisaoClima getPrevisao() {
        return previsao;
    }

    public List<Ocorrencia> getOcorrenciasAtivas() {
        return ocorrenciasAtivas;
    }

    public int calcularIndiceRisco() {
        double limite = regiao.getLimiteChuvaMm() > 0 ? regiao.getLimiteChuvaMm() : 30;

        double chuvaMaxima = previsao != null ? previsao.chuvaMaximaHora(6) : 0;
        double chuvaAcumulada = previsao != null ? previsao.chuvaAcumulada(3) : 0;
        int probabilidade = previsao != null ? previsao.probabilidadeMaxima(6) : 0;

        double pontosChuva = Math.min(50, chuvaMaxima / limite * 50);
        double pontosAcumulado = Math.min(15, chuvaAcumulada / (limite * 1.5) * 15);
        double pontosProbabilidade = Math.min(10, probabilidade * 0.10);
        double pontosHistorico = regiao.pesoSuscetibilidade() * (chuvaMaxima >= 1 ? 1 : 0.5);
        double pontosOcorrencias = 0;
        for (Ocorrencia ocorrencia : ocorrenciasAtivas) {
            pontosOcorrencias += pesoOcorrencia(ocorrencia.getNivelRisco());
        }
        pontosOcorrencias = Math.min(35, pontosOcorrencias);

        double total = pontosChuva + pontosAcumulado + pontosProbabilidade + pontosHistorico + pontosOcorrencias;
        return (int) Math.round(Math.min(100, total));
    }

    public String classificarNivel() {
        int indice = calcularIndiceRisco();

        if (indice >= 75) {
            return "CRITICO";
        }
        if (indice >= 50) {
            return "ALTO";
        }
        if (indice >= 25) {
            return "MEDIO";
        }
        return "BAIXO";
    }

    public List<String> listarFatores() {
        List<String> fatores = new ArrayList<String>();

        if (previsao != null) {
            double chuvaMaxima = previsao.chuvaMaximaHora(6);
            if (chuvaMaxima >= regiao.getLimiteChuvaMm()) {
                fatores.add(String.format(Locale.US, "Chuva prevista de %.1f mm/h passa do limite do bairro (%.0f mm/h)", chuvaMaxima, regiao.getLimiteChuvaMm()));
            } else if (chuvaMaxima >= 1) {
                fatores.add(String.format(Locale.US, "Chuva prevista de até %.1f mm/h nas próximas 6h", chuvaMaxima));
            } else {
                fatores.add("Sem chuva significativa prevista nas próximas 6h");
            }
            if (previsao.chuvaAcumulada(3) >= 10) {
                fatores.add(String.format(Locale.US, "%.1f mm acumulados nas próximas 3h", previsao.chuvaAcumulada(3)));
            }
        }
        if ("ALTA".equals(regiao.getSuscetibilidade())) {
            fatores.add("Bairro com histórico frequente de alagamentos");
        }
        if (!ocorrenciasAtivas.isEmpty()) {
            fatores.add(ocorrenciasAtivas.size() + " ocorrência(s) de alagamento ativa(s) na região");
        }
        return fatores;
    }

    public String gerarRecomendacao() {
        switch (classificarNivel()) {
            case "CRITICO":
                return "Evite sair. Não atravesse ruas alagadas e procure locais altos. Em emergência ligue 199 (Defesa Civil) ou 193.";
            case "ALTO":
                return "Evite a região e as vias marginais. Use a rota segura do NexRain e não estacione perto de córregos.";
            case "MEDIO":
                return "Atenção a pontos de alagamento. Planeje o trajeto antes de sair e acompanhe os alertas.";
            default:
                return "Sem risco relevante no momento.";
        }
    }

    public boolean deveEmitirAlerta() {
        String nivel = classificarNivel();
        return nivel.equals("ALTO") || nivel.equals("CRITICO");
    }

    public Alerta gerarAlertaPrevisao(LocalDateTime agora) {
        String nivel = classificarNivel();

        Alerta alerta = new Alerta();
        alerta.setIdRegiao(regiao.getId());
        alerta.setNivel(nivel);
        alerta.setOrigem("PREVISAO");
        alerta.setTitulo("Risco " + nivel.toLowerCase().replace("medio", "médio").replace("critico", "crítico") + " de alagamento - " + regiao.getNome());
        alerta.setMensagem(String.join(". ", listarFatores()) + ". " + gerarRecomendacao());
        alerta.setDataEmissao(agora);
        alerta.setDataExpiracao(agora.plusHours(6));
        alerta.setAtivo(true);

        if (alerta.getMensagem().length() > 500) {
            alerta.setMensagem(alerta.getMensagem().substring(0, 497) + "...");
        }
        return alerta;
    }

    public static String classificarPorNivelAgua(int nivelAguaCm) {
        if (nivelAguaCm >= 50) {
            return "CRITICO";
        }
        if (nivelAguaCm >= 30) {
            return "ALTO";
        }
        if (nivelAguaCm >= 10) {
            return "MEDIO";
        }
        return "BAIXO";
    }

    public static boolean nivelValido(String nivel) {
        for (String n : NIVEIS) {
            if (n.equals(nivel)) {
                return true;
            }
        }
        return false;
    }

    public static String nivelMaisGrave(String a, String b) {
        return posicao(a) >= posicao(b) ? a : b;
    }

    private static int posicao(String nivel) {
        for (int i = 0; i < NIVEIS.length; i++) {
            if (NIVEIS[i].equals(nivel)) {
                return i;
            }
        }
        return -1;
    }

    private double pesoOcorrencia(String nivel) {
        switch (nivel == null ? "" : nivel) {
            case "CRITICO":
                return 30;
            case "ALTO":
                return 20;
            case "MEDIO":
                return 10;
            default:
                return 4;
        }
    }

    @Override
    public String toString() {
        return "MONITOR DE RISCO " +
                "\nRegião: " + regiao.getNome() +
                "\nÍndice: " + calcularIndiceRisco() +
                "\nNível: " + classificarNivel() +
                "\nFatores: " + listarFatores();
    }
}
