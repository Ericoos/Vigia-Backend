package com.uscs.vigia.model;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@MappedSuperclass
@Getter
@Setter

public class Pessoa {
    protected int id;
    protected String nome;
    protected String email;
    protected String senha;
    protected LocalDate dataNacimento;
    protected enum status;
}
