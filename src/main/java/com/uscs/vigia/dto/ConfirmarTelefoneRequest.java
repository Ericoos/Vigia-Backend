package com.uscs.vigia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmarTelefoneRequest(@NotBlank(message = "O email é obrigatório") @Email(message = "O email é inválido") String email, @NotBlank(message = "O código é obrigatório") @Pattern(regexp = "[0-9]{6}", message = "O código deve ter 6 dígitos") String codigo) {
}
