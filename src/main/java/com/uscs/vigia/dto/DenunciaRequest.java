package com.uscs.vigia.dto;

import com.uscs.vigia.enums.MotivoDenuncia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DenunciaRequest(
        @NotNull(message = "O motivo é obrigatório")
        MotivoDenuncia motivo,

        @NotBlank(message = "A justificativa é obrigatória")
        @Size(min = 10, max = 500, message = "A justificativa deve ter entre 10 e 500 caracteres")
        String descricao
) {
}
