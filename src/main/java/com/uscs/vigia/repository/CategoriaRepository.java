package com.uscs.vigia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uscs.vigia.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}