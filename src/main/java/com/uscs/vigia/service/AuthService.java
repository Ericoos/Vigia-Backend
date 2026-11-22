package com.uscs.vigia.service;

import com.uscs.vigia.dto.CadastroRequest;
import com.uscs.vigia.dto.ConfirmarTelefoneRequest;
import com.uscs.vigia.dto.EsqueciSenhaRequest;
import com.uscs.vigia.dto.LoginRequest;
import com.uscs.vigia.dto.LoginResponse;
import com.uscs.vigia.dto.ReenviarCodigoRequest;
import com.uscs.vigia.dto.RedefinirSenhaRequest;
import com.uscs.vigia.dto.UsuarioResponse;
import com.uscs.vigia.exception.ConflitoException;
import com.uscs.vigia.exception.CodigoInvalidoException;
import com.uscs.vigia.exception.ContaInativaException;
import com.uscs.vigia.exception.CredenciaisInvalidasException;
import com.uscs.vigia.exception.TelefoneNaoConfirmadoException;
import com.uscs.vigia.enums.StatusUsuario;
import com.uscs.vigia.model.Usuario;
import com.uscs.vigia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CodigoService codigoService;
    private final SmsService smsService;

    public UsuarioResponse cadastrar(CadastroRequest cadastroRequest) {
        String email = normalizarEmail(cadastroRequest.email());
        validarEmail(email);
        if (usuarioRepository.existsByTelefone(cadastroRequest.telefone())) {
            throw new ConflitoException("Telefone já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(cadastroRequest.nome());
        usuario.setEmail(email);
        usuario.setTelefone(cadastroRequest.telefone());
        usuario.setDataNascimento(cadastroRequest.dataNascimento());
        usuario.setSenhaHash(passwordEncoder.encode(cadastroRequest.senha()));

        usuario.setStatus(StatusUsuario.USUARIO_ATIVO);
        usuario.setAvaliacao(5.0);
        usuario.setTotalDenunciasRecebidas(0);
        usuario.setDataInicio(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
        usuario.setTelefoneConfirmado(false);
        codigoService.gerar(usuario);

        Usuario salvo = usuarioRepository.save(usuario);
        smsService.enviar(salvo.getTelefone(), "Seu código de confirmação é " + salvo.getCodigoVerificacao());
        return toResponse(salvo);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone(), usuario.getDataNascimento(), usuario.getAvatar(),
                usuario.getAvaliacao() != null ? usuario.getAvaliacao() : 5.0);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void validarEmail(String email) {
        if(usuarioRepository.existsByEmail(email)) {
            throw new ConflitoException("Email já Existe");
        }
    }

    public LoginResponse login(LoginRequest loginRequest) {
        String email = normalizarEmail(loginRequest.email());

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou Senha Inválidos"));

        if(!passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException("E-mail ou Senha Inválidos");
        }

        if(usuario.getStatus() != StatusUsuario.USUARIO_ATIVO) {
            throw new ContaInativaException("A está inativa");
        }
        if (Boolean.FALSE.equals(usuario.getTelefoneConfirmado())) {
            throw new TelefoneNaoConfirmadoException("Confirme seu telefone antes de entrar");
        }

        String token = tokenService.gerarToken(usuario);

        return new LoginResponse(token, toResponse(usuario));
    }

    public void confirmarTelefone(ConfirmarTelefoneRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(request.email()))
                .orElseThrow(() -> new CodigoInvalidoException("Código inválido ou expirado"));

        boolean codigoValido = codigoService.validar(usuario, request.codigo());
        if (codigoValido) {
            usuario.setTelefoneConfirmado(true);
            usuario.setCodigoExpiraEm(null);
            usuario.setTentativasCodigo(null);
        }
        usuarioRepository.save(usuario);
        if (!codigoValido) {
            throw new CodigoInvalidoException("Código inválido ou expirado");
        }
    }

    public void reenviarCodigo(ReenviarCodigoRequest request) {
        usuarioRepository.findByEmail(normalizarEmail(request.email()))
                .filter(usuario -> !Boolean.TRUE.equals(usuario.getTelefoneConfirmado()))
                .filter(codigoService::podeReenviar)
                .ifPresent(usuario -> {
                    codigoService.gerar(usuario);
                    Usuario salvo = usuarioRepository.save(usuario);
                    smsService.enviar(salvo.getTelefone(), "Seu código de confirmação é " + salvo.getCodigoVerificacao());
                });
    }

    public void solicitarRecuperacaoSenha(EsqueciSenhaRequest request) {
        usuarioRepository.findByEmail(normalizarEmail(request.email()))
                .ifPresent(usuario -> {
                    codigoService.gerar(usuario);
                    Usuario salvo = usuarioRepository.save(usuario);
                    log.info("[EMAIL DEV] Para {}: seu código de recuperação é {}", salvo.getEmail(), salvo.getCodigoVerificacao());
                });
    }

    public void redefinirSenha(RedefinirSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(request.email()))
                .orElseThrow(() -> new CodigoInvalidoException("Código inválido ou expirado"));

        boolean codigoValido = codigoService.validar(usuario, request.codigo());
        if (codigoValido) {
            usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        }
        usuarioRepository.save(usuario);
        if (!codigoValido) {
            throw new CodigoInvalidoException("Código inválido ou expirado");
        }
    }
}
