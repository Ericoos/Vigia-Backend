package com.uscs.vigia.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Enumerated(EnumType.STRING)
	private TipoSubcategoria tipoSubCategoria;

	@ManyToOne
	@JoinColumn(name = "categoria_id")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Categoria categoria;

    public Subcategoria(TipoSubcategoria tipoSubCategoria, Categoria categoria) {
        this.tipoSubCategoria = tipoSubCategoria;
        this.categoria = categoria;
    }
}