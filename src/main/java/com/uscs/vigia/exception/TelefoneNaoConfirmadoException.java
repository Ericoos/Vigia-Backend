package com.uscs.vigia.exception;

import org.springframework.http.HttpStatus;

public class TelefoneNaoConfirmadoException extends ApiException {
    public TelefoneNaoConfirmadoException(String mensagem) {
        super(HttpStatus.FORBIDDEN, "Telefone não confirmado", mensagem);
    }
}
