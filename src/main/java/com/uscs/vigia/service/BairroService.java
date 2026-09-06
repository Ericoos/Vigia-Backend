package com.uscs.vigia.service;

import com.uscs.vigia.model.Bairro;
import com.uscs.vigia.repository.BairroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BairroService {

    private final BairroRepository bairroRepository;

    public List<Bairro> listar() {
        return bairroRepository.findAll();
    }
}
