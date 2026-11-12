package com.uscs.vigia.service;

import com.uscs.vigia.dto.AtualizarUsuarioRequest;
import com.uscs.vigia.dto.TrocarSenhaRequest;
import com.uscs.vigia.dto.UsuarioResponse;
import com.uscs.vigia.exception.ResourceNotFoundException;
import com.uscs.vigia.model.Usuario;
import com.uscs.vigia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado: " + id));
        return toResponse(usuario);
    }

    public UsuarioResponse atualizar(Long id, AtualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado: " + id));
        usuario.setNome(request.nome());
        usuario.setTelefone(request.telefone());
        Usuario salvo = usuarioRepository.save(usuario);
        return toResponse(salvo);
    }

    public void trocarSenha(Long id, TrocarSenhaRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado: " + id));
        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone(), usuario.getDataNascimento(), usuario.getAvatar());
    }

}
