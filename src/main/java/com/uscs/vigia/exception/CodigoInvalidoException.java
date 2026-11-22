package com.uscs.vigia.exception;

import org.springframework.http.HttpStatus;

public class CodigoInvalidoException extends ApiException {
    public CodigoInvalidoException(String mensagem) {
        super(HttpStatus.BAD_REQUEST, "Código inválido", mensagem);
    }
}
