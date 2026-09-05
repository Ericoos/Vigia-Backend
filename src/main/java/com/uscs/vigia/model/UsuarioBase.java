package com.uscs.vigia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public class UsuarioBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String nome;
    protected String email;
    protected String senha;
    protected LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    protected TipoUsuario status;
}
