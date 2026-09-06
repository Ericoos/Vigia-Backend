package com.uscs.vigia.model;

public enum TipoCategoria {
    SEGURANCA {
        @Override
        public Double calculaClassificacao(TipoSubcategoria subcategoria) {
            return (TipoCategoria.SEGURANCA.getPeso() + subcategoria.getPeso()) / 10;
        }
    },
    INSEGURANCA {
        @Override
        public Double calculaClassificacao(TipoSubcategoria subcategoria) {
            return (TipoCategoria.INSEGURANCA.getPeso() - subcategoria.getPeso()) / 10;
        }
    };

    private final int peso;

    TipoCategoria() {
        this.peso = 1;
    }

    public int getPeso() {
        return this.peso;
    }

    public abstract Double calculaClassificacao(TipoSubcategoria subcategoria);
}
