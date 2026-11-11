package com.uscs.vigia.controller;

import com.uscs.vigia.dto.CadastroRequest;
import com.uscs.vigia.dto.UsuarioResponse;
import com.uscs.vigia.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest cadastroRequest) {
        return authService.cadastrar(cadastroRequest);
    }

}
