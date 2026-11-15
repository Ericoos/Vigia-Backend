package com.uscs.vigia.exception;

import org.springframework.http.HttpStatus;

public class InactiveAccountException extends ApiException {
    public InactiveAccountException(String message) {
        super(HttpStatus.FORBIDDEN, "Conta inativa", message);
    }
}
