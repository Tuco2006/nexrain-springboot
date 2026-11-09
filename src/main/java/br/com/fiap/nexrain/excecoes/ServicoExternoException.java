package br.com.fiap.nexrain.excecoes;

// usada quando uma API externa (clima, mapas ou IA) nao responde
public class ServicoExternoException extends RuntimeException {

    public ServicoExternoException(String mensagem) {
        super(mensagem);
    }
}
