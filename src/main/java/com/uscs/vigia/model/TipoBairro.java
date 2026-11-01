package com.uscs.vigia.model;

import lombok.Getter;

@Getter
public enum TipoBairro {
    BARCELONA("Barcelona"),
    BOA_VISTA("Boa Vista"),
    CENTRO("Centro"),
    CERAMICA("Cerâmica"),
    FUNDACAO("Fundação"),
    JARDIM_SAO_CAETANO("Jardim São Caetano"),
    MAUA("Mauá"),
    NOVA_GERTY("Nova Gerty"),
    OLIMPICO("Olímpico"),
    OSWALDO_CRUZ("Oswaldo Cruz"),
    PROSPERIDADE("Prosperidade"),
    SANTA_MARIA("Santa Maria"),
    SANTA_PAULA("Santa Paula"),
    SANTO_ANTONIO("Santo Antônio"),
    SAO_JOSE("São José");

    private final String nome;

    TipoBairro(String nome) {
        this.nome = nome;
    }

}
