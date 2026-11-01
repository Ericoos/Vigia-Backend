package com.uscs.vigia.dto;

public record BairroResponse(
        Long id,
        String nome,
        Double classificacaoSeguranca) {
}