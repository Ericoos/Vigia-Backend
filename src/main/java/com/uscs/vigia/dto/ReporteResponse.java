package com.uscs.vigia.dto;

import java.time.OffsetDateTime;

import com.uscs.vigia.model.TipoCategoria;
import com.uscs.vigia.model.TipoSubcategoria;

public record ReporteResponse(
        Long id,
        String titulo,
        String descricao,
        double latitude,
        double longitude,
        OffsetDateTime dataReporte,
        TipoCategoria categoria,
        TipoSubcategoria subcategoria) {
}