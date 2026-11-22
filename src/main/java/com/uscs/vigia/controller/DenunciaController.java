package com.uscs.vigia.controller;

import com.uscs.vigia.dto.DenunciaRequest;
import com.uscs.vigia.dto.DenunciaResponse;
import com.uscs.vigia.service.DenunciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class DenunciaController {
    private final DenunciaService denunciaService;

    @PostMapping("/{idReporte}/denuncias")
    @ResponseStatus(HttpStatus.CREATED)
    public DenunciaResponse denunciar(@PathVariable Long idReporte, @Valid @RequestBody DenunciaRequest request, @AuthenticationPrincipal Jwt jwt) {
        Long idUsuario = Long.valueOf(jwt.getSubject());
        return denunciaService.denunciar(idReporte, request, idUsuario);
    }
}
