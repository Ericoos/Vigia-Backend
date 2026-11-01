package com.uscs.vigia.dto;

import com.uscs.vigia.model.TipoCategoria;
import com.uscs.vigia.model.TipoSubcategoria;

public record SubcategoriaResponse(
        TipoSubcategoria tipoSubCategoria,
        TipoCategoria categoria) {
}