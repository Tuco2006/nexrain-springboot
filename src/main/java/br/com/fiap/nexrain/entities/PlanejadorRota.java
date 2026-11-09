package br.com.fiap.nexrain.entities;

import br.com.fiap.nexrain.api.TrajetoCandidato;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlanejadorRota {

    private static final double FOLGA_DESVIO_METROS = 250;

    private List<Ocorrencia> pontosAlagados;

    public PlanejadorRota() {
        this.pontosAlagados = new ArrayList<Ocorrencia>();
    }

    public PlanejadorRota(List<Ocorrencia> ocorrencias) {
        this.pontosAlagados = new ArrayList<Ocorrencia>();

        if (ocorrencias != null) {
            for (Ocorrencia ocorrencia : ocorrencias) {
                if (ocorrencia.ativa()) {
                    pontosAlagados.add(ocorrencia);
                }
            }
        }
    }

    public List<Ocorrencia> getPontosAlagados() {
        return pontosAlagados;
    }

    public List<Ocorrencia> alagamentosNoTrajeto(TrajetoCandidato trajeto) {
        List<Ocorrencia> atingidas = new ArrayList<Ocorrencia>();
        List<Coordenada> pontos = trajeto.getPontos();

        for (Ocorrencia ocorrencia : pontosAlagados) {
            Coordenada local = ocorrencia.coordenada();

            for (int i = 0; i < pontos.size() - 1; i++) {
                if (local.distanciaAteSegmento(pontos.get(i), pontos.get(i + 1)) <= ocorrencia.raioImpactoMetros()) {
                    atingidas.add(ocorrencia);
                    break;
                }
            }
        }
        return atingidas;
    }

    public double proporcaoRetorno(TrajetoCandidato trajeto) {
        List<Coordenada> pontos = trajeto.getPontos();
        if (pontos.size() < 10) {
            return 0;
        }
        java.util.Set<String> visitados = new java.util.HashSet<String>();
        int repetidos = 0;
        String anterior = "";
        for (Coordenada ponto : pontos) {
            String chave = Math.round(ponto.getLatitude() * 5000) + ":" + Math.round(ponto.getLongitude() * 5000);
            if (!chave.equals(anterior) && !visitados.add(chave)) {
                repetidos++;
            }
            anterior = chave;
        }
        return (double) repetidos / pontos.size();
    }

    public boolean possuiRetorno(TrajetoCandidato trajeto) {
        return proporcaoRetorno(trajeto) > 0.04;
    }

    public double calcularCusto(TrajetoCandidato trajeto) {
        double custo = trajeto.getDuracaoMinutos();

        for (Ocorrencia ocorrencia : alagamentosNoTrajeto(trajeto)) {
            custo += ocorrencia.penalidadeRota();
        }
        return custo;
    }

    public TrajetoCandidato escolherMelhor(List<TrajetoCandidato> candidatos) {
        TrajetoCandidato melhor = null;
        double menorCusto = Double.MAX_VALUE;

        for (TrajetoCandidato candidato : candidatos) {
            double custo = calcularCusto(candidato);
            if (custo < menorCusto) {
                menorCusto = custo;
                melhor = candidato;
            }
        }
        return melhor;
    }

    public List<Coordenada> gerarPontosDesvio(TrajetoCandidato trajeto, Ocorrencia ocorrencia) {
        List<Coordenada> pontos = trajeto.getPontos();
        Coordenada local = ocorrencia.coordenada();

        int indiceMaisPerto = 0;
        double menorDistancia = Double.MAX_VALUE;
        for (int i = 0; i < pontos.size() - 1; i++) {
            double distancia = local.distanciaAteSegmento(pontos.get(i), pontos.get(i + 1));
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                indiceMaisPerto = i;
            }
        }

        Coordenada antes = pontos.get(Math.max(0, indiceMaisPerto - 3));
        Coordenada depois = pontos.get(Math.min(pontos.size() - 1, indiceMaisPerto + 4));
        double direcaoVia = antes.direcaoAte(depois);

        double afastamento = ocorrencia.raioImpactoMetros() + FOLGA_DESVIO_METROS;

        List<Coordenada> desvios = new ArrayList<Coordenada>();
        desvios.add(local.deslocar(afastamento, direcaoVia + 90));
        desvios.add(local.deslocar(afastamento, direcaoVia - 90));
        desvios.add(local.deslocar(afastamento * 2, direcaoVia + 90));
        desvios.add(local.deslocar(afastamento * 2, direcaoVia - 90));
        return desvios;
    }

    public Rota montarRota(String origem, Coordenada coordOrigem, String destino, Coordenada coordDestino,
                           TrajetoCandidato direto, TrajetoCandidato escolhido, LocalDateTime agora) {
        List<Ocorrencia> noDireto = alagamentosNoTrajeto(direto);
        List<Ocorrencia> noEscolhido = alagamentosNoTrajeto(escolhido);

        int evitados = 0;
        for (Ocorrencia ocorrencia : noDireto) {
            if (!noEscolhido.contains(ocorrencia)) {
                evitados++;
            }
        }

        Rota rota = new Rota();
        rota.setOrigem(origem);
        rota.setOrigemLat(coordOrigem.getLatitude());
        rota.setOrigemLon(coordOrigem.getLongitude());
        rota.setDestino(destino);
        rota.setDestinoLat(coordDestino.getLatitude());
        rota.setDestinoLon(coordDestino.getLongitude());
        rota.setDistanciaKm(arredondar(escolhido.getDistanciaMetros() / 1000.0));
        rota.setDuracaoMin(arredondar(escolhido.getDuracaoMinutos()));
        rota.setDistanciaDiretaKm(arredondar(direto.getDistanciaMetros() / 1000.0));
        rota.setDuracaoDiretaMin(arredondar(direto.getDuracaoMinutos()));
        rota.setAlagamentosEvitados(evitados);
        rota.setAlagamentosNoTrajeto(noEscolhido.size());
        rota.setSituacao(definirSituacao(noDireto, noEscolhido));
        rota.setTrajeto(escolhido.getPontos());
        rota.setTrajetoDireto(direto.getPontos());
        rota.setDataCalculo(agora);
        return rota;
    }

    private String definirSituacao(List<Ocorrencia> noDireto, List<Ocorrencia> noEscolhido) {
        if (noEscolhido.isEmpty()) {
            return "SEGURA";
        }
        if (noEscolhido.size() < noDireto.size()) {
            return "RISCO_REDUZIDO";
        }
        return "COM_RISCO";
    }

    private double arredondar(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}
