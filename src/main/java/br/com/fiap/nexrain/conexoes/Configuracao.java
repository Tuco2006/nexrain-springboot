package br.com.fiap.nexrain.conexoes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Configuracao {

    public static String ler(String variavel, String propriedade) {
        String valor = System.getenv(variavel);
        if (valor != null && !valor.isBlank()) {
            return valor.trim();
        }

        valor = lerArquivo("/local.properties", propriedade);
        if (valor != null && !valor.isBlank()) {
            return valor.trim();
        }

        valor = lerArquivo("/application.properties", propriedade);
        if (valor != null && !valor.isBlank()) {
            return valor.trim();
        }
        return null;
    }

    private static String lerArquivo(String arquivo, String propriedade) {
        try (InputStream entrada = Configuracao.class.getResourceAsStream(arquivo)) {
            if (entrada == null) {
                return null;
            }
            Properties propriedades = new Properties();
            propriedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
            return propriedades.getProperty(propriedade);
        } catch (IOException e) {
            System.out.println("Não foi possível ler o arquivo " + arquivo + ": " + e.getMessage());
            return null;
        }
    }
}
