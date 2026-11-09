package br.com.fiap.nexrain.controller;

import br.com.fiap.nexrain.excecoes.RecursoNaoEncontradoException;
import br.com.fiap.nexrain.excecoes.RegraNegocioException;
import br.com.fiap.nexrain.excecoes.ServicoExternoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.util.Map;

@RestControllerAdvice
public class TratadorExcecoes {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> regraNegocio(RegraNegocioException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> requisicaoInvalida(Exception e) {
        System.out.println("Requisição inválida: " + e.getMessage());
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida, confira os dados enviados");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> parametroFaltando(MissingServletRequestParameterException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Informe o parâmetro " + e.getParameterName());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> rotaNaoEncontrada(NoResourceFoundException e) {
        return resposta(HttpStatus.NOT_FOUND, "Rota não encontrada");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> metodoNaoPermitido(HttpRequestMethodNotSupportedException e) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método " + e.getMethod() + " não permitido para essa rota");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> tipoNaoSuportado(HttpMediaTypeNotSupportedException e) {
        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie os dados no formato JSON");
    }

    @ExceptionHandler(ServicoExternoException.class)
    public ResponseEntity<Map<String, Object>> servicoExterno(ServicoExternoException e) {
        return resposta(HttpStatus.BAD_GATEWAY, e.getMessage());
    }

    @ExceptionHandler({SQLException.class, ClassNotFoundException.class})
    public ResponseEntity<Map<String, Object>> erroBanco(Exception e) {
        System.out.println("Erro no banco de dados: " + e.getMessage());
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao acessar o banco de dados");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> erroInesperado(Exception e) {
        System.out.println("Erro inesperado: " + e.getMessage());
        e.printStackTrace();
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor");
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "erro", mensagem));
    }
}
