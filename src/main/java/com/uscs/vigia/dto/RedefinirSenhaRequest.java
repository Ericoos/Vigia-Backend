package com.uscs.vigia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(@NotBlank(message = "O email é obrigatório") @Email(message = "O email é inválido") String email, @NotBlank(message = "O código é obrigatório") @Pattern(regexp = "[0-9]{6}", message = "O código deve ter 6 dígitos") String codigo, @NotBlank(message = "A senha é obrigatória") @Size(min = 8, max = 30, message = "A senha deve ter entre 8 e 30 caracteres") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = "A senha deve ter letra maiúscula, minúscula, número e caractere especial") String novaSenha) {
}
