package br.com.fiap.nexrain.main;

import br.com.fiap.nexrain.api.PrevisaoClima;
import br.com.fiap.nexrain.entities.MonitorRisco;
import br.com.fiap.nexrain.entities.Ocorrencia;
import br.com.fiap.nexrain.entities.Regiao;
import br.com.fiap.nexrain.services.ClimaService;

import java.util.ArrayList;
import java.util.List;

public class TesteMonitorRisco {

    public static void main(String[] args) {

        Regiao centro = new Regiao(1, "Centro / Anhangabaú", "São Paulo", -23.5455, -46.6365, "ALTA", 20);

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setIdRegiao(1);
        ocorrencia.setNivelRisco("ALTO");
        ocorrencia.setStatus("CONFIRMADA");

        List<Ocorrencia> ocorrencias = new ArrayList<Ocorrencia>();
        ocorrencias.add(ocorrencia);

        PrevisaoClima previsao = new ClimaService().buscarPrevisao(centro.getLatitude(), centro.getLongitude());
        System.out.println("Agora: " + previsao.getDescricaoTempo() + ", " + previsao.getTemperatura() + " °C, " +
                previsao.getPrecipitacaoAtual() + " mm");

        MonitorRisco real = new MonitorRisco(centro, previsao, ocorrencias);
        System.out.println("\n--- Previsão real ---\n" + real);

        MonitorRisco temporal = new MonitorRisco(centro, previsao.simularTemporal(45), ocorrencias);
        System.out.println("\n--- Temporal simulado (45 mm/h) ---\n" + temporal);
        System.out.println("Recomendação: " + temporal.gerarRecomendacao());
    }
}
