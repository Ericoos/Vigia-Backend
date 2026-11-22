package com.uscs.vigia.enums;

import java.util.List;

public enum StatusReporte {
    RASCUNHO,
    PUBLICADO,
    EXCLUIDO,
    EDITADO;

    // Status em que o reporte aparece no app (mapa, filtros, bairro, denúncia)
    public static final List<StatusReporte> VISIVEIS = List.of(PUBLICADO, EDITADO);
}