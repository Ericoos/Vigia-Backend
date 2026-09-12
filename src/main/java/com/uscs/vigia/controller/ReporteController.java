package com.uscs.vigia.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uscs.vigia.model.Reporte;
import com.uscs.vigia.service.ReporteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

	private final ReporteService reporteService;

	@GetMapping
	public List<Reporte> listar() {
		return reporteService.listar();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Reporte salvar(@RequestBody Reporte reporte) {
		return reporteService.salvar(reporte);
	}
}