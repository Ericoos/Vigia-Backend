package com.uscs.vigia.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.uscs.vigia.dto.AtualizarUsuarioRequest;
import com.uscs.vigia.dto.DesativarContaRequest;
import com.uscs.vigia.dto.ReporteResponse;
import com.uscs.vigia.dto.TrocarSenhaRequest;
import com.uscs.vigia.dto.UsuarioResponse;
import com.uscs.vigia.exception.RecursoNaoEncontradoException;
import com.uscs.vigia.exception.SenhaIgualException;
import com.uscs.vigia.exception.SenhaIncorretaException;
import com.uscs.vigia.model.Reporte;
import com.uscs.vigia.enums.StatusReporte;
import com.uscs.vigia.enums.StatusUsuario;
import com.uscs.vigia.model.Usuario;
import com.uscs.vigia.repository.ReporteRepository;
import com.uscs.vigia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ReporteRepository reporteRepository;
    private final PasswordEncoder passwordEncoder;

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
        if (request.nome() != null) {
            usuario.setNome(request.nome());
        }
        if (request.telefone() != null) {
            usuario.setTelefone(request.telefone());
        }
        if (request.avatar() != null) {
            usuario.setAvatar(request.avatar());
        }
        Usuario salvo = usuarioRepository.save(usuario);
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
        usuarioRepository.save(usuario);
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
                reporte.getBairro() != null ? reporte.getBairro().getTipoBairro() : null
        );
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone(), usuario.getDataNascimento(), usuario.getAvatar());
    }

}
