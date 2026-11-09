package br.com.fiap.nexrain.services;

import br.com.fiap.nexrain.api.PrevisaoClima;
import br.com.fiap.nexrain.dao.CameraDAO;
import br.com.fiap.nexrain.dao.RotaDAO;
import br.com.fiap.nexrain.dto.PainelResumo;
import br.com.fiap.nexrain.dto.RiscoRegiao;
import br.com.fiap.nexrain.entities.Alerta;
import br.com.fiap.nexrain.entities.Camera;
import br.com.fiap.nexrain.entities.Coordenada;
import br.com.fiap.nexrain.entities.MonitorRisco;
import br.com.fiap.nexrain.entities.Ocorrencia;
import br.com.fiap.nexrain.entities.Regiao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RiscoService {

    private static final Coordenada CENTRO_TEMPORAL = new Coordenada(-23.545, -46.665);
    private static final double RAIO_TEMPORAL_METROS = 4500;

    private final RegiaoService regiaoService = new RegiaoService();
    private final OcorrenciaService ocorrenciaService = new OcorrenciaService();
    private final AlertaService alertaService = new AlertaService();
    private final ClimaService climaService = new ClimaService();

    public List<RiscoRegiao> calcularRiscos(boolean simulacao) throws SQLException, ClassNotFoundException {
        List<RiscoRegiao> riscos = new ArrayList<RiscoRegiao>();
        for (MonitorRisco monitor : montarMonitores(simulacao)) {
            riscos.add(new RiscoRegiao(monitor));
        }
        riscos.sort((a, b) -> Integer.compare(b.getIndice(), a.getIndice()));
        return riscos;
    }

    public List<Alerta> gerarAlertasPrevisao(boolean simulacao) throws SQLException, ClassNotFoundException {
        List<Alerta> gerados = new ArrayList<Alerta>();

        for (MonitorRisco monitor : montarMonitores(simulacao)) {
            if (monitor.deveEmitirAlerta()) {
                Alerta alerta = alertaService.gerarParaPrevisao(monitor);
                if (alerta != null) {
                    gerados.add(alerta);
                }
            }
        }
        return gerados;
    }

    public PainelResumo montarPainel() throws SQLException, ClassNotFoundException {
        List<Ocorrencia> ativas = ocorrenciaService.selecionarAtivas();
        int pendentes = 0;
        for (Ocorrencia ocorrencia : ativas) {
            if ("PENDENTE".equals(ocorrencia.getStatus())) {
                pendentes++;
            }
        }

        int camerasAtivas = 0;
        try (CameraDAO cameraDAO = new CameraDAO()) {
            for (Camera camera : cameraDAO.selecionar()) {
                if (camera.ativa()) {
                    camerasAtivas++;
                }
            }
        }

        int rotas;
        try (RotaDAO rotaDAO = new RotaDAO()) {
            rotas = rotaDAO.selecionar().size();
        }

        return new PainelResumo(regiaoService.selecionar().size(), camerasAtivas, ativas.size(), pendentes,
                alertaService.selecionarVigentes().size(), rotas);
    }

    private List<MonitorRisco> montarMonitores(boolean simulacao) throws SQLException, ClassNotFoundException {
        List<Regiao> regioes = regiaoService.selecionar();
        List<Ocorrencia> ativas = ocorrenciaService.selecionarAtivas();

        List<Coordenada> pontos = new ArrayList<Coordenada>();
        for (Regiao regiao : regioes) {
            pontos.add(regiao.coordenada());
        }
        List<PrevisaoClima> previsoes = regioes.isEmpty() ? new ArrayList<PrevisaoClima>() : climaService.buscarPrevisoes(pontos);

        List<MonitorRisco> monitores = new ArrayList<MonitorRisco>();
        for (int i = 0; i < regioes.size(); i++) {
            PrevisaoClima previsao = previsoes.get(i);
            if (simulacao) {
                previsao = previsao.simularTemporal(intensidadeSimulada(regioes.get(i).coordenada()));
            }
            monitores.add(new MonitorRisco(regioes.get(i), previsao, ativas));
        }
        return monitores;
    }

    private double intensidadeSimulada(Coordenada ponto) {
        double distancia = CENTRO_TEMPORAL.distanciaMetros(ponto);
        double fator = Math.exp(-Math.pow(distancia / RAIO_TEMPORAL_METROS, 2));
        return Math.round((6 + 46 * fator) * 10) / 10.0;
    }
}
