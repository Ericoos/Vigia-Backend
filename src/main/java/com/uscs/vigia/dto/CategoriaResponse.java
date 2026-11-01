package com.uscs.vigia.dto;

import java.util.List;

import com.uscs.vigia.model.TipoCategoria;

public record CategoriaResponse(TipoCategoria tipoCategoria, List<SubcategoriaResponse> subcategorias) {
}