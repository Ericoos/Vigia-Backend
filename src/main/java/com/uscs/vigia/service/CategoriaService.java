package com.uscs.vigia.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.uscs.vigia.model.Categoria;
import com.uscs.vigia.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;

	public List<Categoria> listar() {
		return categoriaRepository.findAll();
	}
}