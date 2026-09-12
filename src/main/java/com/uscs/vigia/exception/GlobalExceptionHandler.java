package com.uscs.vigia.exception;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(corpo(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleRegraNegocio(IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(corpo(HttpStatus.BAD_REQUEST, "Regra de negócio", ex.getMessage(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("requisição inválida");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(corpo(HttpStatus.BAD_REQUEST, "Erro de validação", mensagem, request));
    }

    private Map<String, Object> corpo(HttpStatus status, String erro, String mensagem, HttpServletRequest request) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", OffsetDateTime.now());
        corpo.put("status", status.value());
        corpo.put("erro", erro);
        corpo.put("mensagem", mensagem);
        corpo.put("caminho", request.getRequestURI());
        return corpo;
    }
}