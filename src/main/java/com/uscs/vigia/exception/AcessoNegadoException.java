package com.uscs.vigia.exception;

import org.springframework.http.HttpStatus;

public class AcessoNegadoException extends ApiException {
    public AcessoNegadoException(String message) {
        super(HttpStatus.FORBIDDEN, "Acesso negado", message);
    }
}
