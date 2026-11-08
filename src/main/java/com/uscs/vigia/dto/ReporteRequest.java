package com.uscs.vigia.dto;

import com.uscs.vigia.model.TipoCategoria;
import com.uscs.vigia.model.TipoSubcategoria;

import jakarta.validation.constraints.*;

public record ReporteRequest(
        @Size(min = 5, max = 30, message = "O título deve ter entre 5 e 30 caracteres")
        @NotBlank(message = "O Título é obrigatório.")
        String titulo,

        @Size(min = 20, max = 500, message = "Descrição deve ter entre 20 e 500 caracteres.")
        @NotBlank(message = "Descrição é obrigatória.")
        String descricao,

        @NotNull(message = "Latitude é obrigatória.")
        @DecimalMin(value = "-90.0", message = "latitude mínima é -90.")
        @DecimalMax(value = "90.0", message = "latitude máxima é 90.")
        Double latitude,

        @NotNull(message = "Longitude é obrigatória")
        @DecimalMin(value = "-180.0", message = "longitude mínima é -180.")
        @DecimalMax(value = "180.0", message = "longitude máxima é 180.")
        Double longitude,

        @NotNull(message = "Categoria é obrigatória.")
        TipoCategoria categoria,

        @NotNull(message = "Subcategoria é obrigatória.")
        TipoSubcategoria subcategoria) {
}