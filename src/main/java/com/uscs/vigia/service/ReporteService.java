package com.uscs.vigia.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.uscs.vigia.model.Reporte;
import com.uscs.vigia.repository.ReporteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteService {

	private final ReporteRepository repository;

	public List<Reporte> listar() {
		return repository.findAll();
	}

	public Reporte salvar(Reporte reporte) {
		return repository.save(reporte);
	}
}