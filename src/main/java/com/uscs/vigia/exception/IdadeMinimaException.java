package com.uscs.vigia.exception;

import org.springframework.http.HttpStatus;

public class IdadeMinimaException extends ApiException {
    public IdadeMinimaException(String message) {
        super(HttpStatus.BAD_REQUEST, "Regra de negócio", message);
    }
}
