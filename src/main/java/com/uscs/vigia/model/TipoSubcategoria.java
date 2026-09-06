package com.uscs.vigia.model;

public enum TipoSubcategoria {
    ROUBO(5),
    FURTO(4),
    VANDALISMO(3),
    ILUMINACAO_PUBLICA(2),
    AREA_MOVIMENTADA,
    PRESENCA_POLICIAL,
    ACIDENTE,
    COMERCIO_ATIVO,
    CAMERAS

    public final int peso;

    TipoSubcategoria(int peso) {
        this.peso = peso;
    }
}
