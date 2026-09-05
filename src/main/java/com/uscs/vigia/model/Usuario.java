package com.uscs.vigia.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Usuario extends UsuarioBase {

    private String telefone;
    private LocalDateTime dataCadastro;

}
