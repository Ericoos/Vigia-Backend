package com.uscs.vigia.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.uscs.vigia.dto.SubcategoriaResponse;
import com.uscs.vigia.model.Subcategoria;
import com.uscs.vigia.repository.SubcategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubcategoriaService {

	private final SubcategoriaRepository subcategoriaRepository;

	public List<SubcategoriaResponse> listar() {
		return subcategoriaRepository.findAll()
				.stream()
				.map(SubcategoriaService::toResponse)
				.toList();
	}

	static SubcategoriaResponse toResponse(Subcategoria subcategoria) {
		return new SubcategoriaResponse(
				subcategoria.getTipoSubCategoria(),
				subcategoria.getCategoria().getTipoCategoria());
	}
}