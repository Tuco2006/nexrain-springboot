package br.com.fiap.nexrain.services;

import br.com.fiap.nexrain.api.AnaliseVisaoIA;
import br.com.fiap.nexrain.api.RespostaOpenAi;
import br.com.fiap.nexrain.conexoes.Configuracao;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

// visao computacional com o modelo multimodal da OpenAI (le a imagem da camera)
public class OpenAiService {

    private final static String URL = "https://api.openai.com/v1/chat/completions";

    private final static String CHAVE = Configuracao.ler("OPENAI_API_KEY", "openai.api.key");
    private final static String MODELO_CONFIGURADO = Configuracao.ler("OPENAI_MODEL", "openai.model");
    private final static String MODELO = MODELO_CONFIGURADO != null && !MODELO_CONFIGURADO.isBlank() ? MODELO_CONFIGURADO.trim() : "gpt-4o-mini";

    private final static int TEMPO_LIMITE = 60000;

    private final static String INSTRUCAO_SISTEMA = "Você é o módulo de visão computacional do NexRain, um sistema da Defesa Civil " +
            "que monitora câmeras urbanas para detectar alagamentos. Analise a imagem com rigor técnico, " +
            "responda sempre em português do Brasil e sempre em JSON válido.";

    private final static String PROMPT = "Analise esta imagem de câmera urbana e devolva SOMENTE um JSON neste formato:\n" +
            "{\n" +
            "  \"alagamentoDetectado\": true | false,\n" +
            "  \"nivelAguaEstimadoCm\": número inteiro (0 se não houver água acumulada),\n" +
            "  \"nivelRisco\": \"BAIXO\" | \"MEDIO\" | \"ALTO\" | \"CRITICO\",\n" +
            "  \"confianca\": número de 0 a 100,\n" +
            "  \"viaTransitavel\": true | false,\n" +
            "  \"descricao\": \"1 ou 2 frases objetivas sobre o que aparece na imagem\"\n" +
            "}\n\n" +
            "Regras:\n" +
            "- Pista apenas molhada ou com poças pequenas NÃO é alagamento (alagamentoDetectado = false, nível BAIXO).\n" +
            "- Estime a altura da água usando referências visíveis: guia da calçada (~15 cm), meio da roda de um carro (~30 cm), " +
            "porta do carro (~50 cm), joelho de uma pessoa (~50 cm).\n" +
            "- nivelRisco: BAIXO abaixo de 10 cm, MEDIO de 10 a 29 cm, ALTO de 30 a 49 cm, CRITICO a partir de 50 cm ou com correnteza forte.\n" +
            "- viaTransitavel = false quando carros de passeio não conseguem passar com segurança.";

    public boolean configurada() {
        return CHAVE != null && !CHAVE.isBlank();
    }

    // imagem = URL publica ou "data:image/jpeg;base64,..."
    public AnaliseVisaoIA analisarImagem(String imagem) throws IOException {
        if (!configurada()) {
            throw new IOException("Chave da OpenAI não configurada");
        }

        Gson gson = new Gson();

        HttpPost request = new HttpPost(URL);
        request.setHeader("Authorization", "Bearer " + CHAVE.trim());
        request.setEntity(new StringEntity(gson.toJson(montarCorpo(imagem)), ContentType.APPLICATION_JSON));

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TEMPO_LIMITE)
                .setSocketTimeout(TEMPO_LIMITE)
                .build();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
             CloseableHttpResponse response = httpClient.execute(request)) {

            HttpEntity entity = response.getEntity();
            String result = entity != null ? EntityUtils.toString(entity, "UTF-8") : "";

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IOException("Erro na API da OpenAI: " + response.getStatusLine().getStatusCode() + " " + result);
            }

            RespostaOpenAi resposta = gson.fromJson(result, RespostaOpenAi.class);

            if (resposta == null || resposta.getConteudo() == null) {
                throw new IOException("Resposta vazia da OpenAI");
            }

            AnaliseVisaoIA analise = gson.fromJson(resposta.getConteudo(), AnaliseVisaoIA.class);
            analise.setOrigem("openai");
            return analise;
        }
    }

    private JsonObject montarCorpo(String imagem) {
        JsonObject sistema = new JsonObject();
        sistema.addProperty("role", "system");
        sistema.addProperty("content", INSTRUCAO_SISTEMA);

        JsonObject texto = new JsonObject();
        texto.addProperty("type", "text");
        texto.addProperty("text", PROMPT);

        JsonObject url = new JsonObject();
        url.addProperty("url", imagem);
        url.addProperty("detail", "low");

        JsonObject figura = new JsonObject();
        figura.addProperty("type", "image_url");
        figura.add("image_url", url);

        JsonArray conteudo = new JsonArray();
        conteudo.add(texto);
        conteudo.add(figura);

        JsonObject usuario = new JsonObject();
        usuario.addProperty("role", "user");
        usuario.add("content", conteudo);

        JsonArray mensagens = new JsonArray();
        mensagens.add(sistema);
        mensagens.add(usuario);

        JsonObject formato = new JsonObject();
        formato.addProperty("type", "json_object");

        JsonObject corpo = new JsonObject();
        corpo.addProperty("model", MODELO);
        corpo.addProperty("temperature", 0.2);
        corpo.add("response_format", formato);
        corpo.add("messages", mensagens);
        return corpo;
    }
}
