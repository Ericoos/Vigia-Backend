package com.uscs.vigia.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Subcategoria {

	@Id
    @Enumerated(EnumType.STRING)
	private TipoSubcategoria tipoSubCategoria;

	@ManyToOne
	@JoinColumn(name = "categoria_id")
	private Categoria categoria;

    public Subcategoria(TipoSubcategoria tipoSubCategoria, Categoria categoria) {
        this.tipoSubCategoria = tipoSubCategoria;
        this.categoria = categoria;
    }
}