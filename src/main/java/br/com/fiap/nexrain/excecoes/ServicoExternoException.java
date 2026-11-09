package br.com.fiap.nexrain.excecoes;

public class ServicoExternoException extends RuntimeException {

    public ServicoExternoException(String mensagem) {
        super(mensagem);
    }
}
