package com.uscs.vigia.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarUsuarioRequest(
        @Pattern(regexp = "(?s).*\\P{javaWhitespace}.*", message = "O nome não pode ficar em branco")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String nome,

        @Pattern(regexp = "^[1-9]{2}(9\\d{8}|[2-5]\\d{7})$",
                message = "Telefone inválido: envie só os números com DDD (ex.: 11987654321)")
        String telefone,

        String avatar) {
}
