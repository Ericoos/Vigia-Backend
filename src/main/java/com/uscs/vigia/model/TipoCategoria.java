package com.uscs.vigia.model;

import lombok.Getter;

@Getter
public enum TipoCategoria {
    SEGURANCA {
        @Override
        public Double calculaClassificacao(TipoSubcategoria subcategoria) {
            return (TipoCategoria.SEGURANCA.getPeso() + subcategoria.getPeso()) / 100.0;
        }
    },
    INSEGURANCA {
        @Override
        public Double calculaClassificacao(TipoSubcategoria subcategoria) {
            return (TipoCategoria.INSEGURANCA.getPeso() - subcategoria.getPeso()) / 100.0;
        }
    };

    private final Double peso;

    TipoCategoria() {
        this.peso = 1.0;
    }

    public abstract Double calculaClassificacao(TipoSubcategoria subcategoria);
}
