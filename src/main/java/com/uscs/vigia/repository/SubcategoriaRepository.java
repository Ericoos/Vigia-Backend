package com.uscs.vigia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uscs.vigia.model.Subcategoria;
import com.uscs.vigia.model.TipoSubcategoria;

public interface SubcategoriaRepository extends JpaRepository<Subcategoria, TipoSubcategoria> {
}