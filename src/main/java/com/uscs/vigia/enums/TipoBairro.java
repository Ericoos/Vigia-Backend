package com.uscs.vigia.enums;

import java.text.Normalizer;
import java.util.Optional;

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

    public static Optional<TipoBairro> porNome(String texto) {
        if (texto == null) {
            return Optional.empty();
        }

        String nomeNormalizado = normalizarNome(texto);
        if ("Sao Caetano".equalsIgnoreCase(nomeNormalizado)) {
            return Optional.of(JARDIM_SAO_CAETANO);
        }

        for (TipoBairro bairro : values()) {
            if (normalizarNome(bairro.getNome()).equalsIgnoreCase(nomeNormalizado)) {
                return Optional.of(bairro);
            }
        }
        return Optional.empty();
    }

    private static String normalizarNome(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

}
