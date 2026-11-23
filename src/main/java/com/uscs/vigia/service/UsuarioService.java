package com.uscs.vigia.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.uscs.vigia.dto.AtualizarUsuarioRequest;
import com.uscs.vigia.dto.ConfirmarEmailRequest;
import com.uscs.vigia.dto.DesativarContaRequest;
import com.uscs.vigia.dto.ReporteResponse;
import com.uscs.vigia.dto.TrocarSenhaRequest;
import com.uscs.vigia.dto.TrocarEmailRequest;
import com.uscs.vigia.dto.UsuarioResponse;
import com.uscs.vigia.exception.RecursoNaoEncontradoException;
import com.uscs.vigia.exception.ConflitoException;
import com.uscs.vigia.exception.CodigoInvalidoException;
import com.uscs.vigia.exception.SenhaIgualException;
import com.uscs.vigia.exception.SenhaIncorretaException;
import com.uscs.vigia.model.Reporte;
import com.uscs.vigia.enums.StatusReporte;
import com.uscs.vigia.enums.FinalidadeCodigo;
import com.uscs.vigia.enums.StatusUsuario;
import com.uscs.vigia.model.Usuario;
import com.uscs.vigia.repository.ReporteRepository;
import com.uscs.vigia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ReporteRepository reporteRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodigoService codigoService;
    private final SmsService smsService;

    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado: " + id));
        return toResponse(usuario);
    }

    public UsuarioResponse atualizar(Long id, AtualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado: " + id));
        boolean telefoneAlterado = request.telefone() != null && !request.telefone().equals(usuario.getTelefone());
        if (telefoneAlterado && usuarioRepository.existsByTelefone(request.telefone())) {
            throw new ConflitoException("Telefone já cadastrado");
        }
        if (request.nome() != null) {
            usuario.setNome(request.nome());
        }
        if (telefoneAlterado) {
            usuario.setTelefone(request.telefone());
            usuario.setTelefoneConfirmado(false);
            codigoService.gerar(usuario, FinalidadeCodigo.TELEFONE);
        }
        if (request.avatar() != null) {
            usuario.setAvatar(request.avatar());
        }
        Usuario salvo = usuarioRepository.save(usuario);
        if (telefoneAlterado) {
            smsService.enviar(salvo.getTelefone(), "Seu código de confirmação é " + salvo.getCodigoVerificacao());
        }
        return toResponse(salvo);
    }

    public void trocarSenha(Long id, TrocarSenhaRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado: " + id));
        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        if (passwordEncoder.matches(request.novaSenha(), usuario.getSenhaHash())) {
            throw new SenhaIgualException("A nova senha deve ser diferente da senha atual");
        }
        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        Usuario salvo = usuarioRepository.save(usuario);
        avisarSenhaAlterada(salvo);
    }

    public void solicitarTrocaEmail(Long idUsuario, TrocarEmailRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado: " + idUsuario));
        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new SenhaIncorretaException("Senha incorreta");
        }

        String novoEmail = normalizarEmail(request.novoEmail());
        validarEmailDisponivel(novoEmail, idUsuario);
        usuario.setEmailPendente(novoEmail);
        codigoService.gerar(usuario, FinalidadeCodigo.TROCA_EMAIL);
        Usuario salvo = usuarioRepository.save(usuario);
        log.info("[EMAIL DEV] Para {}: seu código para confirmar o novo e-mail é {}", salvo.getEmailPendente(), salvo.getCodigoVerificacao());
    }

    public UsuarioResponse confirmarEmail(Long idUsuario, ConfirmarEmailRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado: " + idUsuario));
        String novoEmail = usuario.getEmailPendente();
        if (novoEmail == null) {
            throw new CodigoInvalidoException("Não há troca de e-mail pendente");
        }

        boolean codigoValido = codigoService.validar(usuario, request.codigo(), FinalidadeCodigo.TROCA_EMAIL);
        usuarioRepository.save(usuario);
        if (!codigoValido) {
            throw new CodigoInvalidoException("Código inválido ou expirado");
        }

        validarEmailDisponivel(novoEmail, idUsuario);
        usuario.setEmail(novoEmail);
        usuario.setEmailPendente(null);
        Usuario salvo = usuarioRepository.save(usuario);
        return toResponse(salvo);
    }

    public void desativarConta(Long id, DesativarContaRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado: " + id));
        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new SenhaIncorretaException("Senha incorreta");
        }
        usuario.setStatus(StatusUsuario.USUARIO_INATIVO);
        usuario.setDataFim(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
        usuarioRepository.save(usuario);
    }

    public List<ReporteResponse> listarReportes(Long idUsuario) {
        return reporteRepository.findAllByUsuario_IdAndStatusNotOrderByDataReporteDesc(idUsuario, StatusReporte.EXCLUIDO)
                .stream()
                .map(this::toReporteResponse)
                .toList();
    }

    private ReporteResponse toReporteResponse(Reporte reporte) {
        return new ReporteResponse(
                reporte.getIdReporte(),
                reporte.getTitulo(),
                reporte.getDescricao(),
                reporte.getLatitude(),
                reporte.getLongitude(),
                reporte.getDataReporte(),
                reporte.getCategoria().getTipoCategoria(),
                reporte.getSubcategoria().getTipoSubCategoria(),
                reporte.getUsuario() != null ? reporte.getUsuario().getId() : null,
                reporte.getUsuario() != null ? reporte.getUsuario().getNome() : null,
                reporte.getEndereco(),
                reporte.getBairro() != null ? reporte.getBairro().getTipoBairro() : null,
                reporte.getStatus() == StatusReporte.EDITADO
        );
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void validarEmailDisponivel(String email, Long idUsuario) {
        boolean usadoPorOutraConta = usuarioRepository.findByEmail(email)
                .filter(usuario -> !usuario.getId().equals(idUsuario))
                .isPresent();
        if (usadoPorOutraConta) {
            throw new ConflitoException("E-mail já cadastrado");
        }
    }

    private void avisarSenhaAlterada(Usuario usuario) {
        try {
            log.info("[EMAIL DEV] Para {}: sua senha foi alterada em {}. Se não foi você, recupere sua senha.",
                    usuario.getEmail(), LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
        } catch (RuntimeException falha) {
            log.warn("Falha ao registrar aviso de senha alterada para {}", usuario.getEmail(), falha);
        }
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone(), usuario.getDataNascimento(), usuario.getAvatar(),
                usuario.getAvaliacao() != null ? usuario.getAvaliacao() : 5.0);
    }

}
