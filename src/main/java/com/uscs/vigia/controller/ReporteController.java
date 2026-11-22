package com.uscs.vigia.controller;

import java.util.List;

import com.uscs.vigia.dto.ReporteRequest;
import com.uscs.vigia.dto.ReporteResponse;
import com.uscs.vigia.enums.TipoCategoria;
import com.uscs.vigia.enums.TipoBairro;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.uscs.vigia.service.ReporteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    public List<ReporteResponse> listar(@RequestParam(required = false) TipoCategoria categoria, @RequestParam(required = false) TipoBairro bairro) {
        return reporteService.listar(categoria, bairro);
    }

    @GetMapping("/{id}")
    public ReporteResponse buscarPorId(@PathVariable Long id) {
        return reporteService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReporteResponse criar(@Valid @RequestBody ReporteRequest reporteRequest, @AuthenticationPrincipal Jwt jwt) {
        Long idUsuario = Long.valueOf(jwt.getSubject());
        return reporteService.criar(reporteRequest, idUsuario);
    }
}
