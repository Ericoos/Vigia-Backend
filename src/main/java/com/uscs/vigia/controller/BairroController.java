package com.uscs.vigia.controller;

import com.uscs.vigia.model.Bairro;
import com.uscs.vigia.service.BairroService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bairro")
@RequiredArgsConstructor
public class BairroController {

    public final BairroService bairroService;

    @GetMapping
    public List<Bairro> listar() {
        return bairroService.listar();
    }
}
