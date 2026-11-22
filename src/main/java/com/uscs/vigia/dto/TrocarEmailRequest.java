package com.uscs.vigia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TrocarEmailRequest(@NotBlank(message = "O novo email é obrigatório") @Email(message = "O email é inválido") String novoEmail, @NotBlank(message = "A senha é obrigatória") String senha) {
    public TrocarEmailRequest {
        if (novoEmail != null) {
            novoEmail = novoEmail.trim();
        }
    }
}
