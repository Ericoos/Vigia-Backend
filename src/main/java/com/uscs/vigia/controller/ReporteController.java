package com.uscs.vigia.controller;

import java.util.List;

import com.uscs.vigia.dto.ReporteRequest;
import com.uscs.vigia.dto.ReporteResponse;
import com.uscs.vigia.repository.ReporteRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.uscs.vigia.model.Reporte;
import com.uscs.vigia.service.ReporteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

	private final ReporteService reporteService;

	@GetMapping
	public List<ReporteResponse> listar() {
		return reporteService.listar();
	}

    @GetMapping("/{id}")
    public ReporteResponse listarPorId(@PathVariable Long id) {
        return reporteService.listarPorId(id);
    }

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ReporteResponse criar(@Valid @RequestBody ReporteRequest reporteRequest) {
		return reporteService.criar(reporteRequest);
	}
}