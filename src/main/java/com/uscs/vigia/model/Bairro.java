package com.uscs.vigia.model;

import com.uscs.vigia.enums.TipoBairro;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Bairro {

    @Id
    @Enumerated(EnumType.STRING)
    private TipoBairro tipoBairro;

    private Double classificacaoSeguranca;

    public Bairro(TipoBairro tipoBairro) {
        this.tipoBairro = tipoBairro;
    }
}