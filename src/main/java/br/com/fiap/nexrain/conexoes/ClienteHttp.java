package br.com.fiap.nexrain.conexoes;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

// conexao com as APIs publicas usadas pelo NexRain (Open-Meteo, OSRM, Nominatim, imagens das cameras)
public class ClienteHttp {

    // Nominatim e Wikimedia pedem um User-Agent que identifique a aplicacao
    public static final String USER_AGENT = "NexRain/1.0 (projeto academico FIAP - Global Solution)";

    public static String get(String url, int tempoLimite) throws IOException {
        byte[] conteudo = baixar(url, tempoLimite);
        return new String(conteudo, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static byte[] baixar(String url, int tempoLimite) throws IOException {
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept-Language", "pt-BR,pt;q=0.9");

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(tempoLimite)
                .setSocketTimeout(tempoLimite)
                .build();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).disableCookieManagement().build();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            byte[] conteudo = entity != null ? EntityUtils.toByteArray(entity) : new byte[0];

            if (status != 200) {
                throw new IOException("Resposta " + status + " de " + url);
            }
            return conteudo;
        }
    }
}
