package com.uscs.vigia.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
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
                .filter(r -> r.getStatus() == StatusReporte.PUBLICADO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reporte não encontrado: " + id));
        return toResponse(reporte);
    }

    @Transactional
    public ReporteResponse criar(ReporteRequest reporteRequest) {
        Categoria categoria = categoriaRepository.findById(reporteRequest.categoria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria não encontrada: " + reporteRequest.categoria()));

        Subcategoria subcategoria = subcategoriaRepository.findById(reporteRequest.subcategoria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subcategoria não encontrada: " + reporteRequest.subcategoria()));

        if (subcategoria.getCategoria().getTipoCategoria() != categoria.getTipoCategoria()) {
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
        return toResponse(reporteSalvo);
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