package com.uscs.vigia.exception;

import org.springframework.http.HttpStatus;

public class BairroInvalidoException extends ApiException {
    public BairroInvalidoException(String message) {
        super(HttpStatus.BAD_REQUEST, "Regra de negócio", message);
    }
}
