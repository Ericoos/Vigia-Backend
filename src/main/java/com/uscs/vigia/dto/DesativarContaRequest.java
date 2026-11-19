package com.uscs.vigia.dto;

import jakarta.validation.constraints.NotBlank;

public record DesativarContaRequest(@NotBlank(message = "A senha é obrigatória") String senha) {
}
