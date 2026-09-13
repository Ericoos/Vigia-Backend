package com.uscs.vigia.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import com.uscs.vigia.dto.ReporteRequest;
import com.uscs.vigia.dto.ReporteResponse;
import com.uscs.vigia.exception.ResourceNotFoundException;
import com.uscs.vigia.model.*;
import com.uscs.vigia.repository.CategoriaRepository;
import com.uscs.vigia.repository.SubcategoriaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.uscs.vigia.repository.ReporteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteService {

	private final ReporteRepository reporteRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;

    @Transactional(readOnly = true)
	public List<ReporteResponse> listar() {
		return reporteRepository.findAllByStatus(StatusReporte.PUBLICADO)
                .stream()
                .map(this::toResponse)
                .toList();
	}

    @Transactional(readOnly = true)
    public ReporteResponse listarPorId(Long id) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reporte não encontrado: " + id));
        return toResponse(reporte);
    }

	public Reporte salvar(Reporte reporte) {
		return reporteRepository.save(reporte);
	}

    @Transactional
    public ReporteResponse criar(ReporteRequest reporteRequest) {
        Categoria categoria = categoriaRepository.findById(reporteRequest.idCategoria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria não encontrada: " + reporteRequest.idCategoria()));

        Subcategoria subcategoria = subcategoriaRepository.findById(reporteRequest.idSubcategoria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subcategoria não encontrada: " + reporteRequest.idSubcategoria()));

        if (!subcategoria.getCategoria().getId().equals(categoria.getId())) {
            throw new IllegalArgumentException(
                    "A subcategoria não pertence a categoria informada");
        }

        Reporte reporte = new Reporte();

        reporte.setTitulo(reporteRequest.titulo());
        reporte.setDescricao(reporteRequest.descricao());
        reporte.setLatitude(reporteRequest.latitude());
        reporte.setLongitude(reporteRequest.longitude());

        reporte.setCategoria(categoria);
        reporte.setSubcategoria(subcategoria);

        reporte.setStatus(StatusReporte.PUBLICADO);
        reporte.setDataReporte(OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")));

        Reporte reporteSalvo = reporteRepository.save(reporte);
        return toResponse(reporte);
    }

    private ReporteResponse toResponse(Reporte reporte) {
        return new ReporteResponse(
                reporte.getIdReporte(),
                reporte.getTitulo(),
                reporte.getDescricao(),
                reporte.getLatitude(),
                reporte.getLongitude(),
                reporte.getDataReporte(),
                reporte.getCategoria().getTipoCategoria(),
                reporte.getSubcategoria().getTipoSubCategoria()
        );
    }
}