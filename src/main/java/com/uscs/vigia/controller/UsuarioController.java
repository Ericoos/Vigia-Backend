package com.uscs.vigia.controller;

import java.util.List;

import com.uscs.vigia.dto.AtualizarUsuarioRequest;
import com.uscs.vigia.dto.ReporteResponse;
import com.uscs.vigia.dto.TrocarSenhaRequest;
import com.uscs.vigia.dto.UsuarioResponse;
import com.uscs.vigia.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public UsuarioResponse buscarPerfil(@AuthenticationPrincipal Jwt jwt) {
        Long idUsuario = Long.valueOf(jwt.getSubject());
        return usuarioService.buscarPorId(idUsuario);
    }

    @PutMapping("/me")
    public UsuarioResponse atualizar(@Valid @RequestBody AtualizarUsuarioRequest request, @AuthenticationPrincipal Jwt jwt) {
        Long idUsuario = Long.valueOf(jwt.getSubject());
        return usuarioService.atualizar(idUsuario, request);
    }

    @PutMapping("/me/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trocarSenha(@Valid @RequestBody TrocarSenhaRequest request, @AuthenticationPrincipal Jwt jwt) {
        Long idUsuario = Long.valueOf(jwt.getSubject());
        usuarioService.trocarSenha(idUsuario, request);
    }

    @GetMapping("/me/reportes")
    public List<ReporteResponse> listarReportes(@AuthenticationPrincipal Jwt jwt) {
        Long idUsuario = Long.valueOf(jwt.getSubject());
        return usuarioService.listarReportes(idUsuario);
    }
}
