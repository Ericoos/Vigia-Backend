package com.uscs.vigia.dto;

import java.util.List;

import com.uscs.vigia.enums.TipoBairro;

public record BairroDetalheResponse(TipoBairro tipoBairro, String nome, Double classificacaoSeguranca, List<ReporteResponse> reportes) {
}
