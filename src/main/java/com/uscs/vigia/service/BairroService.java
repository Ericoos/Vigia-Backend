package com.uscs.vigia.service;

import com.uscs.vigia.dto.BairroResponse;
import com.uscs.vigia.exception.ResourceNotFoundException;
import com.uscs.vigia.model.Bairro;
import com.uscs.vigia.model.TipoBairro;
import com.uscs.vigia.repository.BairroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BairroService {

    private final BairroRepository bairroRepository;

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
        return toResponse(bairro);
    }

    private BairroResponse toResponse(Bairro bairro) {
        return new BairroResponse(bairro.getTipoBairro(), bairro.getTipoBairro().getNome(), bairro.getClassificacaoSeguranca());
    }
}
