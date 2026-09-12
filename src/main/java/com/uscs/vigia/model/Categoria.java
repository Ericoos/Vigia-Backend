package com.uscs.vigia.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Enumerated(EnumType.STRING)
	private TipoCategoria tipoCategoria;

	@OneToMany(mappedBy = "categoria", fetch = FetchType.EAGER)
	private List<Subcategoria> subcategorias = new ArrayList<>();

    public Categoria(TipoCategoria tipoCategoria) {
        this.tipoCategoria = tipoCategoria;
    }
}