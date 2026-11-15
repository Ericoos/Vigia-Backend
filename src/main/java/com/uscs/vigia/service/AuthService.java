package com.uscs.vigia.service;

import com.uscs.vigia.dto.CadastroRequest;
import com.uscs.vigia.dto.LoginRequest;
import com.uscs.vigia.dto.LoginResponse;
import com.uscs.vigia.dto.UsuarioResponse;
import com.uscs.vigia.exception.ConflictException;
import com.uscs.vigia.exception.InactiveAccountException;
import com.uscs.vigia.exception.InvalidCredentialsException;
import com.uscs.vigia.model.TipoUsuario;
import com.uscs.vigia.model.Usuario;
import com.uscs.vigia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UsuarioResponse cadastrar(CadastroRequest cadastroRequest) {
        String email = emailNormalizer(cadastroRequest.email());
        emailValidator(email);

        Usuario usuario = new Usuario();
        usuario.setNome(cadastroRequest.nome());
        usuario.setEmail(email);
        usuario.setTelefone(cadastroRequest.telefone());
        usuario.setDataNascimento(cadastroRequest.dataNascimento());
        usuario.setSenhaHash(passwordEncoder.encode(cadastroRequest.senha()));

        usuario.setStatus(TipoUsuario.USUARIO_ATIVO);
        usuario.setDataInicio(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));

        Usuario salvo = usuarioRepository.save(usuario);
        return toResponse(salvo);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone(), usuario.getDataNascimento(), usuario.getAvatar());
    }

    private String emailNormalizer(String email) {
        return email.trim().toLowerCase();
    }

    private void emailValidator(String email) {
        if(usuarioRepository.existsByEmail(email)) {
            throw new ConflictException("Email já Existe");
        }
    }

    public LoginResponse login(LoginRequest loginRequest) {
        String email = emailNormalizer(loginRequest.email());

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("E-mail ou Senha Inválidos"));

        if(!passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())) {
            throw new InvalidCredentialsException("E-mail ou Senha Inválidos");
        }

        if(usuario.getStatus() != TipoUsuario.USUARIO_ATIVO) {
            throw new InactiveAccountException("A está inativa");
        }

        String token = tokenService.gerarToken(usuario);

        return new LoginResponse(token, toResponse(usuario));
    }
}
