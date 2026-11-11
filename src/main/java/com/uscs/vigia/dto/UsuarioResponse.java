package com.uscs.vigia.dto;

import java.time.LocalDate;

public record UsuarioResponse(Long id, String nome, String email, String telefone, LocalDate dataNascimento, String avatar) {
}
