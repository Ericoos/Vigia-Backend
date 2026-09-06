package com.uscs.vigia.model;

import lombok.Getter;

@Getter
public enum TipoSubcategoria {
    ROUBO(5.0),
    FURTO(4.0),
    VANDALISMO(3.0),
    ILUMINACAO_PUBLICA(2.0),
    AREA_MOVIMENTADA(5.0),
    PRESENCA_POLICIAL(5.0),
    ACIDENTE(3.0),
    COMERCIO_ATIVO(3.0),
    CAMERAS(3.0);

    private final Double peso;

    TipoSubcategoria(Double peso) {
        this.peso = peso;
    }
}
