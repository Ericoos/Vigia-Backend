package com.uscs.vigia.exception;

import org.springframework.http.HttpStatus;

public class SenhaIncorretaException extends ApiException {
    public SenhaIncorretaException(String message) {
        super(HttpStatus.BAD_REQUEST, "Regra de negócio", message);
    }
}
