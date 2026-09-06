package com.uscs.vigia.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.uscs.vigia.model.Subcategoria;
import com.uscs.vigia.repository.SubcategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubcategoriaService {

	private final SubcategoriaRepository repository;

	public List<Subcategoria> listar() {
		return repository.findAll();
	}
}