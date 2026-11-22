package com.uscs.vigia.controller;

import com.uscs.vigia.dto.CadastroRequest;
import com.uscs.vigia.dto.ConfirmarTelefoneRequest;
import com.uscs.vigia.dto.EsqueciSenhaRequest;
import com.uscs.vigia.dto.LoginRequest;
import com.uscs.vigia.dto.LoginResponse;
import com.uscs.vigia.dto.ReenviarCodigoRequest;
import com.uscs.vigia.dto.RedefinirSenhaRequest;
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

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/confirmar-telefone")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmarTelefone(@Valid @RequestBody ConfirmarTelefoneRequest request) {
        authService.confirmarTelefone(request);
    }

    @PostMapping("/reenviar-codigo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reenviarCodigo(@Valid @RequestBody ReenviarCodigoRequest request) {
        authService.reenviarCodigo(request);
    }

    @PostMapping("/esqueci-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void solicitarRecuperacaoSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        authService.solicitarRecuperacaoSenha(request);
    }

    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        authService.redefinirSenha(request);
    }
}
