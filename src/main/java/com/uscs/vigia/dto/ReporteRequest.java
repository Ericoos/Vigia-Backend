package com.uscs.vigia.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReporteRequest(
        @NotBlank(message = "titulo é obrigatório")
        String titulo,

        @NotBlank(message = "descricao é obrigatória")
        String descricao,

        @NotNull(message = "latitude é obrigatória")
        @DecimalMin(value = "-90.0", message = "latitude mínima é -90")
        @DecimalMax(value = "90.0", message = "latitude máxima é 90")
        Double latitude,

        @NotNull(message = "longitude é obrigatória")
        @DecimalMin(value = "-180.0", message = "longitude mínima é -180")
        @DecimalMax(value = "180.0", message = "longitude máxima é 180")
        Double longitude,

        @NotNull(message = "idCategoria é obrigatório")
        Long idCategoria,

        @NotNull(message = "idSubcategoria é obrigatório")
        Long idSubcategoria) {
}