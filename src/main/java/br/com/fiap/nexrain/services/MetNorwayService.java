package br.com.fiap.nexrain.services;

import br.com.fiap.nexrain.api.PrevisaoClima;
import br.com.fiap.nexrain.api.PrevisaoHora;
import br.com.fiap.nexrain.conexoes.ClienteHttp;
import br.com.fiap.nexrain.entities.Coordenada;
import br.com.fiap.nexrain.entities.Ocorrencia;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// previsao reserva: API publica do instituto meteorologico da Noruega (https://api.met.no), sem chave
public class MetNorwayService {

    private final static String URL = "https://api.met.no/weatherapi/locationforecast/2.0/complete";
    private final static int TEMPO_LIMITE = 15000;
    private final static int HORAS_PREVISAO = 24;
    private final static DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public PrevisaoClima buscarPrevisao(Coordenada ponto) throws IOException {
        String url = String.format(Locale.US, "%s?lat=%.4f&lon=%.4f", URL, ponto.getLatitude(), ponto.getLongitude());
        JsonObject json = JsonParser.parseString(ClienteHttp.get(url, TEMPO_LIMITE)).getAsJsonObject();
        JsonArray serie = json.getAsJsonObject("properties").getAsJsonArray("timeseries");

        LocalDateTime agora = LocalDateTime.now(Ocorrencia.FUSO);
        LocalDateTime horaAtual = agora.truncatedTo(ChronoUnit.HOURS);

        PrevisaoClima previsao = new PrevisaoClima();
        previsao.setLatitude(ponto.getLatitude());
        previsao.setLongitude(ponto.getLongitude());
        previsao.setHoraReferencia(agora.format(FORMATO_HORA));
        previsao.setFonte("MET Norway");

        List<PrevisaoHora> proximas = new ArrayList<PrevisaoHora>();
        boolean primeira = true;

        for (JsonElement item : serie) {
            JsonObject passo = item.getAsJsonObject();
            // horario vem em UTC, converte pro horario de Sao Paulo
            LocalDateTime hora = LocalDateTime.ofInstant(Instant.parse(passo.get("time").getAsString()), Ocorrencia.FUSO);
            if (hora.isBefore(horaAtual)) {
                continue;
            }

            JsonObject dados = passo.getAsJsonObject("data");
            JsonObject proximaHora = dados.getAsJsonObject("next_1_hours");
            if (proximaHora == null) {
                break; // a partir de ~2 dias a API so tem blocos de 6 horas
            }

            JsonObject detalhes = proximaHora.getAsJsonObject("details");
            double chuva = numero(detalhes, "precipitation_amount");
            int probabilidade = detalhes.has("probability_of_precipitation")
                    ? (int) Math.round(numero(detalhes, "probability_of_precipitation"))
                    : estimarProbabilidade(chuva);

            if (primeira) {
                JsonObject instante = dados.getAsJsonObject("instant").getAsJsonObject("details");
                previsao.setTemperatura(numero(instante, "air_temperature"));
                previsao.setUmidade((int) Math.round(numero(instante, "relative_humidity")));
                previsao.setPrecipitacaoAtual(chuva);
                String simbolo = proximaHora.has("summary") ? proximaHora.getAsJsonObject("summary").get("symbol_code").getAsString() : "";
                previsao.setCodigoTempo(codigoWmo(simbolo));
                primeira = false;
            }

            proximas.add(new PrevisaoHora(hora.format(FORMATO_HORA), chuva, probabilidade));
            if (proximas.size() >= HORAS_PREVISAO) {
                break;
            }
        }

        if (proximas.isEmpty()) {
            throw new IOException("MET Norway não devolveu previsão horária");
        }
        previsao.setProximasHoras(proximas);
        return previsao;
    }

    // o MET Norway nem sempre manda a probabilidade, entao estima pelo volume previsto
    private int estimarProbabilidade(double chuvaMm) {
        if (chuvaMm <= 0) {
            return 5;
        }
        return (int) Math.min(95, 40 + chuvaMm * 15);
    }

    // converte o simbolo do MET Norway ("lightrainshowers_day", "heavyrainandthunder"...) para o codigo WMO usado no sistema
    private int codigoWmo(String simbolo) {
        String s = simbolo.toLowerCase();
        if (s.contains("thunder")) return 95;
        if (s.contains("snow") || s.contains("sleet")) return 71;
        if (s.contains("showers")) return s.contains("heavy") ? 82 : 80;
        if (s.contains("rain")) return s.contains("heavy") ? 65 : s.contains("light") ? 61 : 63;
        if (s.contains("fog")) return 45;
        if (s.startsWith("cloudy")) return 3;
        if (s.startsWith("partlycloudy")) return 2;
        if (s.startsWith("fair")) return 1;
        return 0;
    }

    private double numero(JsonObject json, String campo) {
        JsonElement valor = json.get(campo);
        return valor == null || valor.isJsonNull() ? 0 : valor.getAsDouble();
    }
}
