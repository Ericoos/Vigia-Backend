package com.uscs.vigia.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.uscs.vigia.dto.CategoriaResponse;
import com.uscs.vigia.model.Categoria;
import com.uscs.vigia.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;

	public List<CategoriaResponse> listar() {
		return categoriaRepository.findAll()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	private CategoriaResponse toResponse(Categoria categoria) {
		return new CategoriaResponse(
				categoria.getTipoCategoria(),
				categoria.getSubcategorias().stream()
						.map(SubcategoriaService::toResponse)
						.toList());
	}
}