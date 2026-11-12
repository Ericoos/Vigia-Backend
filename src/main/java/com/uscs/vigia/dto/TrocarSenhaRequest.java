package com.uscs.vigia.dto;

import jakarta.validation.constraints.NotBlank;

public record TrocarSenhaRequest(@NotBlank(message = "A senha atual é obrigatória") String senhaAtual, @NotBlank(message = "A nova senha é obrigatória") String novaSenha) {
}
