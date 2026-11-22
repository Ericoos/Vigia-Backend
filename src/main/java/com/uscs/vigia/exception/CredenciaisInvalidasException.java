package com.uscs.vigia.exception;

import org.springframework.http.HttpStatus;

public class CredenciaisInvalidasException extends ApiException {
    public CredenciaisInvalidasException(String message) {
        super(HttpStatus.UNAUTHORIZED, "Não autorizado", message);
    }
}
