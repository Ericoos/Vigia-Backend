package com.uscs.vigia.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CadastroRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email é inválido")
        String email,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(regexp = "^[1-9]{2}(9\\d{8}|[2-5]\\d{7})$",
                message = "Telefone inválido: envie só os números com DDD (ex.: 11987654321)")
        String telefone,

        @NotNull(message = "A data de nascimento é obrigatória")
        @Past(message = "A data de nascimento deve ser no passado")
        LocalDate dataNascimento,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 30, message = "A senha deve ter entre 8 e 30 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "A senha deve ter letra maiúscula, minúscula, número e caractere especial")
        String senha
) {
}
