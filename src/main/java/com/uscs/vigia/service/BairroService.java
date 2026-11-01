package com.uscs.vigia.service;

import com.uscs.vigia.dto.BairroResponse;
import com.uscs.vigia.model.Bairro;
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

    private BairroResponse toResponse(Bairro bairro) {
        return new BairroResponse(
                bairro.getId(),
                bairro.getNome(),
                bairro.getClassificacaoSeguranca());
    }
}
