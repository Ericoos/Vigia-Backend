package com.uscs.vigia.dto;

import java.util.List;

import com.uscs.vigia.enums.TipoCategoria;

public record CategoriaResponse(TipoCategoria tipoCategoria, List<SubcategoriaResponse> subcategorias) {
}