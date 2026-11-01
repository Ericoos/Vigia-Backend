package com.uscs.vigia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uscs.vigia.model.Categoria;
import com.uscs.vigia.model.TipoCategoria;

public interface CategoriaRepository extends JpaRepository<Categoria, TipoCategoria> {
}