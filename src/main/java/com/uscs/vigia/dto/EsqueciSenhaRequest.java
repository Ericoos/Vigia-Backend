package com.uscs.vigia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaRequest(@NotBlank(message = "O email é obrigatório") @Email(message = "O email é inválido") String email) {
}
