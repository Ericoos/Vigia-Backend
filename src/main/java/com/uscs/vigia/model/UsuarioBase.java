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
public abstract class UsuarioBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String senhaHash;
    private LocalDate dataNascimento;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private TipoUsuario status;
}
