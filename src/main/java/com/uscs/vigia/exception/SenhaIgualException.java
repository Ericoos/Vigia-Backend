package com.uscs.vigia.exception;

import org.springframework.http.HttpStatus;

public class SenhaIgualException extends ApiException {
    public SenhaIgualException(String message) {
        super(HttpStatus.BAD_REQUEST, "Regra de negócio", message);
    }
}
