package com.uscs.vigia.enums;

import lombok.Getter;

@Getter
public enum TipoCategoria {
    SEGURANCA {
        @Override
        public Double calcularClassificacao(TipoSubcategoria subcategoria) {
            return (getPeso() + subcategoria.getPeso()) / 50.0;
        }
    },
    INSEGURANCA {
        @Override
        public Double calcularClassificacao(TipoSubcategoria subcategoria) {
            return (getPeso() - subcategoria.getPeso()) / 50.0;
        }
    };

    private final Double peso;

    TipoCategoria() {
        this.peso = 1.0;
    }

    public abstract Double calcularClassificacao(TipoSubcategoria subcategoria);
}
