package br.com.fiap.nexrain.services;

import br.com.fiap.nexrain.api.PrevisaoClima;
import br.com.fiap.nexrain.api.PrevisaoHora;
import br.com.fiap.nexrain.conexoes.ClienteHttp;
import br.com.fiap.nexrain.entities.Coordenada;
import br.com.fiap.nexrain.excecoes.RegraNegocioException;
import br.com.fiap.nexrain.excecoes.ServicoExternoException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// previsao do tempo real pela API gratuita Open-Meteo (https://open-meteo.com), sem chave
public class ClimaService {

    private final static String URL = "https://api.open-meteo.com/v1/forecast";
    private final static int TEMPO_LIMITE = 15000;
    private final static int HORAS_PREVISAO = 24;

    // guarda as previsoes por 10 minutos pra nao chamar a API toda hora
    private final static long VALIDADE_CACHE_MS = 10 * 60 * 1000;
    private final static Map<String, PrevisaoClima> CACHE = new ConcurrentHashMap<String, PrevisaoClima>();
    private final static Map<String, Long> CACHE_HORARIO = new ConcurrentHashMap<String, Long>();

    // quando a Open-Meteo devolve "limite de uso", ela fica pausada por um tempo
    private static volatile long openMeteoPausadaAte = 0;

    private final MetNorwayService metNorway = new MetNorwayService();

    // Previsao de um ponto
    public PrevisaoClima buscarPrevisao(double latitude, double longitude) {
        Coordenada ponto = new Coordenada(latitude, longitude);
        if (!ponto.valida()) {
            throw new RegraNegocioException("Latitude e longitude inválidas");
        }
        return buscarPrevisoes(List.of(ponto)).get(0);
    }

    // Previsao de varios pontos numa unica chamada (a Open-Meteo aceita lista de coordenadas)
    public List<PrevisaoClima> buscarPrevisoes(List<Coordenada> pontos) {
        List<PrevisaoClima> resultado = new ArrayList<PrevisaoClima>();
        List<Coordenada> faltando = new ArrayList<Coordenada>();
        long agora = System.currentTimeMillis();

        for (Coordenada ponto : pontos) {
            String chave = chave(ponto);
            Long horario = CACHE_HORARIO.get(chave);
            if (horario == null || agora - horario > VALIDADE_CACHE_MS) {
                faltando.add(ponto);
            }
        }

        if (!faltando.isEmpty()) {
            try {
                List<PrevisaoClima> novas = consultarFontes(faltando);
                for (int i = 0; i < faltando.size(); i++) {
                    String chave = chave(faltando.get(i));
                    CACHE.put(chave, novas.get(i));
                    CACHE_HORARIO.put(chave, agora);
                }
            } catch (ServicoExternoException e) {
                // as duas fontes falharam: usa a ultima previsao guardada, se tiver de todos os pontos
                for (Coordenada ponto : faltando) {
                    if (!CACHE.containsKey(chave(ponto))) {
                        throw e;
                    }
                }
                System.out.println("Usando previsão guardada em cache (fontes indisponíveis)");
            }
        }

        for (Coordenada ponto : pontos) {
            resultado.add(CACHE.get(chave(ponto)));
        }
        return resultado;
    }

    // Open-Meteo e a fonte principal. Se ela recusar (limite de uso por IP, comum em servidor gratuito
    // compartilhado como o Render), usa a previsao do MET Norway, que tambem e publica e sem chave
    private List<PrevisaoClima> consultarFontes(List<Coordenada> pontos) {
        if (System.currentTimeMillis() > openMeteoPausadaAte) {
            try {
                return consultarApi(pontos);
            } catch (IOException | RuntimeException e) {
                System.out.println("Falha ao consultar a Open-Meteo: " + e.getMessage());
                // limite de uso (429) pausa por 30 minutos; outras falhas (fora do ar, lentidao) por 10
                boolean limiteDeUso = e.getMessage() != null && e.getMessage().contains("Resposta 429");
                openMeteoPausadaAte = System.currentTimeMillis() + (limiteDeUso ? 30 : 10) * 60 * 1000;
            }
        }

        try {
            List<PrevisaoClima> previsoes = new ArrayList<PrevisaoClima>();
            for (Coordenada ponto : pontos) {
                previsoes.add(metNorway.buscarPrevisao(ponto));
            }
            return previsoes;
        } catch (IOException | RuntimeException e) {
            System.out.println("Falha ao consultar o MET Norway: " + e.getMessage());
            throw new ServicoExternoException("Não foi possível consultar a previsão do tempo agora");
        }
    }

    private List<PrevisaoClima> consultarApi(List<Coordenada> pontos) throws IOException {
        StringBuilder latitudes = new StringBuilder();
        StringBuilder longitudes = new StringBuilder();

        for (Coordenada ponto : pontos) {
            if (latitudes.length() > 0) {
                latitudes.append(',');
                longitudes.append(',');
            }
            latitudes.append(String.format(Locale.US, "%.4f", ponto.getLatitude()));
            longitudes.append(String.format(Locale.US, "%.4f", ponto.getLongitude()));
        }

        String url = URL + "?latitude=" + latitudes + "&longitude=" + longitudes +
                "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code" +
                "&hourly=precipitation,precipitation_probability" +
                "&forecast_days=2&timezone=America%2FSao_Paulo";

        JsonElement json = JsonParser.parseString(ClienteHttp.get(url, TEMPO_LIMITE));
        List<PrevisaoClima> previsoes = new ArrayList<PrevisaoClima>();

        // com um ponto so a API devolve um objeto, com varios devolve uma lista
        if (json.isJsonArray()) {
            for (JsonElement item : json.getAsJsonArray()) {
                previsoes.add(converter(item.getAsJsonObject()));
            }
        } else {
            previsoes.add(converter(json.getAsJsonObject()));
        }
        return previsoes;
    }

    private PrevisaoClima converter(JsonObject json) {
        PrevisaoClima previsao = new PrevisaoClima();
        previsao.setLatitude(json.get("latitude").getAsDouble());
        previsao.setLongitude(json.get("longitude").getAsDouble());

        JsonObject atual = json.getAsJsonObject("current");
        String horaAtual = atual.get("time").getAsString();
        previsao.setHoraReferencia(horaAtual);
        previsao.setTemperatura(numero(atual, "temperature_2m"));
        previsao.setUmidade((int) numero(atual, "relative_humidity_2m"));
        previsao.setPrecipitacaoAtual(numero(atual, "precipitation"));
        previsao.setCodigoTempo((int) numero(atual, "weather_code"));

        JsonObject horaria = json.getAsJsonObject("hourly");
        JsonArray horas = horaria.getAsJsonArray("time");
        JsonArray chuvas = horaria.getAsJsonArray("precipitation");
        JsonArray probabilidades = horaria.getAsJsonArray("precipitation_probability");

        // a lista comeca a meia-noite, entao pula ate a hora atual ("2026-11-09T14:15" -> "2026-11-09T14")
        String prefixoHoraAtual = horaAtual.substring(0, 13);
        int inicio = 0;
        for (int i = 0; i < horas.size(); i++) {
            if (horas.get(i).getAsString().startsWith(prefixoHoraAtual)) {
                inicio = i;
                break;
            }
        }

        List<PrevisaoHora> proximas = new ArrayList<PrevisaoHora>();
        for (int i = inicio; i < Math.min(horas.size(), inicio + HORAS_PREVISAO); i++) {
            double chuva = chuvas.get(i).isJsonNull() ? 0 : chuvas.get(i).getAsDouble();
            int probabilidade = probabilidades.get(i).isJsonNull() ? 0 : probabilidades.get(i).getAsInt();
            proximas.add(new PrevisaoHora(horas.get(i).getAsString(), chuva, probabilidade));
        }
        previsao.setProximasHoras(proximas);
        return previsao;
    }

    private double numero(JsonObject json, String campo) {
        JsonElement valor = json.get(campo);
        return valor == null || valor.isJsonNull() ? 0 : valor.getAsDouble();
    }

    private String chave(Coordenada ponto) {
        return String.format(Locale.US, "%.3f,%.3f", ponto.getLatitude(), ponto.getLongitude());
    }
}
