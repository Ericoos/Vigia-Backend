package com.uscs.vigia.service;

import com.uscs.vigia.dto.BairroDetalheResponse;
import com.uscs.vigia.dto.BairroResponse;
import com.uscs.vigia.dto.ReporteResponse;
import com.uscs.vigia.exception.RecursoNaoEncontradoException;
import com.uscs.vigia.model.Bairro;
import com.uscs.vigia.model.Reporte;
import com.uscs.vigia.enums.StatusReporte;
import com.uscs.vigia.enums.TipoBairro;
import com.uscs.vigia.repository.BairroRepository;
import com.uscs.vigia.repository.DenunciaRepository;
import com.uscs.vigia.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BairroService {

    private final BairroRepository bairroRepository;
    private final ReporteRepository reporteRepository;
    private final DenunciaRepository denunciaRepository;

    public List<BairroResponse> listar() {
        return bairroRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BairroDetalheResponse buscarPorTipo(TipoBairro tipoBairro) {
        Bairro bairro = bairroRepository.findById(tipoBairro)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Bairro não encontrado: " + tipoBairro));
        recalcularClassificacao(bairro);
        List<ReporteResponse> reportes = reporteRepository.findAllByBairroAndStatusIn(bairro, StatusReporte.VISIVEIS)
                .stream()
                .map(this::toReporteResponse)
                .toList();
        return new BairroDetalheResponse(bairro.getTipoBairro(), bairro.getTipoBairro().getNome(), notaExibida(bairro.getClassificacaoSeguranca()), reportes);
    }

    public void recalcularClassificacao(Bairro bairro) {
        List<Reporte> reportes = reporteRepository.findAllByBairroAndStatusIn(bairro, StatusReporte.VISIVEIS);

        double classificacao = 5.0;
        for (Reporte reporte : reportes) {
            if (denunciaRepository.countByReporteAlvo_IdReporte(reporte.getIdReporte()) >= 3) {
                continue;
            }
            classificacao += reporte.getCategoria().getTipoCategoria()
                    .calcularClassificacao(reporte.getSubcategoria().getTipoSubCategoria());
        }

        bairro.setClassificacaoSeguranca(classificacao);
        bairroRepository.save(bairro);
    }

    private ReporteResponse toReporteResponse(Reporte reporte) {
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
                reporte.getBairro() != null ? reporte.getBairro().getTipoBairro() : null,
                reporte.getStatus() == StatusReporte.EDITADO
        );
    }

    private BairroResponse toResponse(Bairro bairro) {
        return new BairroResponse(bairro.getTipoBairro(), bairro.getTipoBairro().getNome(), notaExibida(bairro.getClassificacaoSeguranca()));
    }

    private Double notaExibida(Double notaInterna) {
        if (notaInterna == null) {
            return 5.0;
        }
        double limitada = Math.max(0.0, Math.min(5.0, notaInterna));
        return Math.round(limitada * 10) / 10.0;
    }
}
