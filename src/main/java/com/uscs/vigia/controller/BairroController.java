package com.uscs.vigia.controller;

import com.uscs.vigia.dto.BairroResponse;
import com.uscs.vigia.model.TipoBairro;
import com.uscs.vigia.service.BairroService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bairro")
@RequiredArgsConstructor
public class BairroController {

    private final BairroService bairroService;

    @GetMapping
    public List<BairroResponse> listar() {
        return bairroService.listar();
    }

    @GetMapping("/{tipoBairro}")
    public BairroResponse buscarPorTipo(@PathVariable TipoBairro tipoBairro) {
        return bairroService.buscarPorTipo(tipoBairro);
    }
}
