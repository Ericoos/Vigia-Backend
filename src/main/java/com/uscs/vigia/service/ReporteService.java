package com.uscs.vigia.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import com.uscs.vigia.dto.ReporteRequest;
import com.uscs.vigia.dto.ReporteResponse;
import com.uscs.vigia.enums.StatusReporte;
import com.uscs.vigia.enums.TipoBairro;
import com.uscs.vigia.enums.TipoCategoria;
import com.uscs.vigia.exception.BairroInvalidoException;
import com.uscs.vigia.exception.RecursoNaoEncontradoException;
import com.uscs.vigia.model.*;
import com.uscs.vigia.repository.BairroRepository;
import com.uscs.vigia.repository.CategoriaRepository;
import com.uscs.vigia.repository.SubcategoriaRepository;
import com.uscs.vigia.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;
    private final BairroRepository bairroRepository;
    private final BairroService bairroService;

    @Transactional(readOnly = true)
    public List<ReporteResponse> listar(TipoCategoria categoria, TipoBairro bairro) {
        List<Reporte> reportes;
        if (categoria != null && bairro != null) {
            reportes = reporteRepository.findAllByStatusAndCategoria_TipoCategoriaAndBairro_TipoBairro(StatusReporte.PUBLICADO, categoria, bairro);
        } else if (categoria != null) {
            reportes = reporteRepository.findAllByStatusAndCategoria_TipoCategoria(StatusReporte.PUBLICADO, categoria);
        } else if (bairro != null) {
            reportes = reporteRepository.findAllByStatusAndBairro_TipoBairro(StatusReporte.PUBLICADO, bairro);
        } else {
            reportes = reporteRepository.findAllByStatus(StatusReporte.PUBLICADO);
        }
        return reportes.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReporteResponse buscarPorId(Long id) {
        Reporte reporte = reporteRepository.findById(id)
                .filter(r -> r.getStatus() == StatusReporte.PUBLICADO)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Reporte não encontrado: " + id));
        return toResponse(reporte);
    }

    @Transactional
    public ReporteResponse criar(ReporteRequest reporteRequest, Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado, ID: " + idUsuario));

        Categoria categoria = categoriaRepository.findById(reporteRequest.categoria())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada: " + reporteRequest.categoria()));

        Subcategoria subcategoria = subcategoriaRepository.findById(reporteRequest.subcategoria())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
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
        reporte.setEndereco(reporteRequest.endereco());

        if (reporteRequest.bairro() != null) {
            TipoBairro tipoBairro = TipoBairro.porNome(reporteRequest.bairro())
                    .orElseThrow(() -> new BairroInvalidoException(
                            "Bairro inválido: " + reporteRequest.bairro()));
            Bairro bairro = bairroRepository.findById(tipoBairro)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Bairro não encontrado: " + tipoBairro));
            reporte.setBairro(bairro);
        }

        reporte.setCategoria(categoria);
        reporte.setSubcategoria(subcategoria);
        reporte.setUsuario(usuario);

        reporte.setStatus(StatusReporte.PUBLICADO);
        reporte.setDataReporte(OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")));

        Reporte reporteSalvo = reporteRepository.save(reporte);
        if (reporteSalvo.getBairro() != null) {
            bairroService.recalcularClassificacao(reporteSalvo.getBairro());
        }
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
                reporte.getSubcategoria().getTipoSubCategoria(),
                reporte.getUsuario() != null ? reporte.getUsuario().getId() : null,
                reporte.getUsuario() != null ? reporte.getUsuario().getNome() : null,
                reporte.getEndereco(),
                reporte.getBairro() != null ? reporte.getBairro().getTipoBairro() : null
        );
    }
}
