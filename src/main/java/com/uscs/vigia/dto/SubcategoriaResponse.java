package com.uscs.vigia.dto;

import com.uscs.vigia.enums.TipoCategoria;
import com.uscs.vigia.enums.TipoSubcategoria;

public record SubcategoriaResponse(TipoSubcategoria tipoSubCategoria, TipoCategoria categoria) {
}