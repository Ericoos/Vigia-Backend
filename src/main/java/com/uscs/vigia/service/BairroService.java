package com.uscs.vigia.service;

import com.uscs.vigia.dto.BairroResponse;
import com.uscs.vigia.exception.ResourceNotFoundException;
import com.uscs.vigia.model.Bairro;
import com.uscs.vigia.model.Reporte;
import com.uscs.vigia.model.StatusReporte;
import com.uscs.vigia.model.TipoBairro;
import com.uscs.vigia.repository.BairroRepository;
import com.uscs.vigia.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BairroService {

    private final BairroRepository bairroRepository;
    private final ReporteRepository reporteRepository;

    public List<BairroResponse> listar() {
        return bairroRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BairroResponse buscarPorTipo(TipoBairro tipoBairro) {
        Bairro bairro = bairroRepository.findById(tipoBairro)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bairro não encontrado: " + tipoBairro));
        recalcularClassificacao(bairro);
        return toResponse(bairro);
    }

    public void recalcularClassificacao(Bairro bairro) {
        List<Reporte> reportes = reporteRepository.findAllByBairroAndStatus(bairro, StatusReporte.PUBLICADO);

        Double classificacao = null;
        if (!reportes.isEmpty()) {
            classificacao = 0.0;
            for (Reporte reporte : reportes) {
                classificacao += reporte.getCategoria().getTipoCategoria()
                        .calculaClassificacao(reporte.getSubcategoria().getTipoSubCategoria());
            }
        }

        bairro.setClassificacaoSeguranca(classificacao);
        bairroRepository.save(bairro);
    }

    private BairroResponse toResponse(Bairro bairro) {
        return new BairroResponse(bairro.getTipoBairro(), bairro.getTipoBairro().getNome(), bairro.getClassificacaoSeguranca());
    }
}
