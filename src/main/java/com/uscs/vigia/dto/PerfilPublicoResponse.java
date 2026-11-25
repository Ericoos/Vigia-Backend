package com.uscs.vigia.dto;

import java.util.List;

public record PerfilPublicoResponse(Long id, String nome, String avatar, Double avaliacao, List<ReporteResponse> reportes) {
}
