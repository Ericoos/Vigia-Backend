package com.uscs.vigia.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uscs.vigia.model.Categoria;
import com.uscs.vigia.service.CategoriaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

	private final CategoriaService service;

	@GetMapping
	public List<Categoria> listar() {
		return service.listar();
	}
}