package br.com.fiap.nexrain.services;

import br.com.fiap.nexrain.api.TrajetoCandidato;
import br.com.fiap.nexrain.conexoes.ClienteHttp;
import br.com.fiap.nexrain.dto.LocalEncontrado;
import br.com.fiap.nexrain.entities.Coordenada;
import br.com.fiap.nexrain.excecoes.RegraNegocioException;
import br.com.fiap.nexrain.excecoes.ServicoExternoException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapaService {

    private final static String[] SERVIDORES_ROTA = {
            "https://router.project-osrm.org/route/v1/driving/",
            "https://routing.openstreetmap.de/routed-car/route/v1/driving/"
    };
    private final static String URL_ENDERECO = "https://nominatim.openstreetmap.org/search";

    private final static String AREA_SAO_PAULO = "-46.83,-23.36,-46.36,-23.80";

    private final static int TEMPO_LIMITE = 20000;

    public List<TrajetoCandidato> buscarRotas(Coordenada origem, Coordenada destino) {
        String pontos = formatar(origem) + ";" + formatar(destino);
        List<TrajetoCandidato> rotas = consultarRotas(pontos, "alternatives=3");

        for (int i = 0; i < rotas.size(); i++) {
            rotas.get(i).setDescricao(i == 0 ? "Rota mais rápida" : "Alternativa " + i + " do OSRM");
        }
        return rotas;
    }

    public TrajetoCandidato buscarRotaPassandoPor(Coordenada origem, Coordenada via, Coordenada destino) {
        String pontos = formatar(origem) + ";" + formatar(via) + ";" + formatar(destino);
        List<TrajetoCandidato> rotas = consultarRotas(pontos, "alternatives=false&continue_straight=true");

        if (rotas.isEmpty()) {
            return null;
        }
        rotas.get(0).setDescricao("Desvio calculado pelo NexRain");
        return rotas.get(0);
    }

    public List<LocalEncontrado> geocodificar(String endereco) {
        if (endereco == null || endereco.trim().length() < 3) {
            throw new RegraNegocioException("Digite pelo menos 3 letras do endereço");
        }

        String url = URL_ENDERECO + "?format=jsonv2&limit=5&countrycodes=br&viewbox=" + AREA_SAO_PAULO +
                "&q=" + URLEncoder.encode(endereco.trim(), StandardCharsets.UTF_8);

        try {
            JsonArray itens = JsonParser.parseString(ClienteHttp.get(url, TEMPO_LIMITE)).getAsJsonArray();
            List<LocalEncontrado> locais = new ArrayList<LocalEncontrado>();

            for (JsonElement item : itens) {
                JsonObject local = item.getAsJsonObject();
                locais.add(new LocalEncontrado(
                        local.get("display_name").getAsString(),
                        local.get("lat").getAsDouble(),
                        local.get("lon").getAsDouble()));
            }
            return locais;
        } catch (IOException | RuntimeException e) {
            System.out.println("Falha ao consultar o Nominatim: " + e.getMessage());
            throw new ServicoExternoException("Não foi possível buscar o endereço agora");
        }
    }

    public LocalEncontrado localizar(String endereco) {
        List<LocalEncontrado> locais = geocodificar(endereco);
        if (locais.isEmpty()) {
            throw new RegraNegocioException("Endereço não encontrado: " + endereco);
        }
        return locais.get(0);
    }

    private List<TrajetoCandidato> consultarRotas(String pontos, String parametros) {
        String erro = "";

        for (String servidor : SERVIDORES_ROTA) {
            String url = servidor + pontos + "?overview=full&geometries=geojson&" + parametros;
            try {
                JsonObject json = JsonParser.parseString(ClienteHttp.get(url, TEMPO_LIMITE)).getAsJsonObject();

                if (!"Ok".equals(json.get("code").getAsString())) {
                    throw new RegraNegocioException("Não existe rota de carro entre esses dois pontos");
                }

                List<TrajetoCandidato> rotas = new ArrayList<TrajetoCandidato>();
                for (JsonElement item : json.getAsJsonArray("routes")) {
                    rotas.add(converter(item.getAsJsonObject()));
                }
                return rotas;
            } catch (RegraNegocioException e) {
                throw e;
            } catch (IOException | RuntimeException e) {
                erro = e.getMessage();
                System.out.println("Servidor de rotas " + servidor + " falhou: " + erro);
            }
        }
        throw new ServicoExternoException("Serviço de rotas indisponível no momento");
    }

    private TrajetoCandidato converter(JsonObject rota) {
        List<Coordenada> pontos = new ArrayList<Coordenada>();

        for (JsonElement ponto : rota.getAsJsonObject("geometry").getAsJsonArray("coordinates")) {
            JsonArray par = ponto.getAsJsonArray();
            pontos.add(new Coordenada(par.get(1).getAsDouble(), par.get(0).getAsDouble()));
        }

        return new TrajetoCandidato(null, rota.get("distance").getAsDouble(), rota.get("duration").getAsDouble(), pontos);
    }

    private String formatar(Coordenada c) {
        return String.format(Locale.US, "%.6f,%.6f", c.getLongitude(), c.getLatitude());
    }
}
