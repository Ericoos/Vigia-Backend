package com.uscs.vigia.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uscs.vigia.model.Subcategoria;
import com.uscs.vigia.service.SubcategoriaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subcategorias")
@RequiredArgsConstructor
public class SubcategoriaController {

	private final SubcategoriaService service;

	@GetMapping
	public List<Subcategoria> listar() {
		return service.listar();
	}
}