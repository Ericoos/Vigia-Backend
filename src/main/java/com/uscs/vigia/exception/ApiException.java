package com.uscs.vigia.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException{
    private final HttpStatus status;
    private final String erro;

    protected ApiException(HttpStatus status, String erro, String message) {
        super(message);
        this.status = status;
        this.erro = erro;
    }
}
