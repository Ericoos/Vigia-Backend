package com.uscs.vigia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TrocarSenhaRequest(@NotBlank(message = "A senha atual é obrigatória") String senhaAtual, @NotBlank(message = "A nova senha é obrigatória") @Size(min = 8, max = 30, message = "A senha deve ter entre 8 e 30 caracteres") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = "A senha deve ter letra maiúscula, minúscula, número e caractere especial") String novaSenha) {
}
