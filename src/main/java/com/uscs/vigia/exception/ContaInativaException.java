package com.uscs.vigia.exception;

import org.springframework.http.HttpStatus;

public class ContaInativaException extends ApiException {
    public ContaInativaException(String message) {
        super(HttpStatus.FORBIDDEN, "Conta inativa", message);
    }
}
