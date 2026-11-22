package com.uscs.vigia.dto;

import com.uscs.vigia.enums.TipoBairro;

public record BairroResponse(TipoBairro tipoBairro, String nome, Double classificacaoSeguranca) {
}