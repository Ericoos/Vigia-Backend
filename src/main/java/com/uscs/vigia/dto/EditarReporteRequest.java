package com.uscs.vigia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditarReporteRequest(
        @NotBlank(message = "A descrição é obrigatória")
        @Size(min = 20, max = 500, message = "A descrição deve ter entre 20 e 500 caracteres")
        String descricao
) {
}
