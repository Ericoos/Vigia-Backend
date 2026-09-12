package com.uscs.vigia.service;

import java.util.List;

import com.uscs.vigia.model.Categoria;
import org.springframework.stereotype.Service;

import com.uscs.vigia.model.Subcategoria;
import com.uscs.vigia.repository.SubcategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubcategoriaService {

	private final SubcategoriaRepository subcategoriaRepository;

	public List<Subcategoria> listar() {
		return subcategoriaRepository.findAll();
	}
}