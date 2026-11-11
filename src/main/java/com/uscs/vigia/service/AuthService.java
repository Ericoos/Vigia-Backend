package com.uscs.vigia.service;

import com.uscs.vigia.dto.CadastroRequest;
import com.uscs.vigia.dto.UsuarioResponse;
import com.uscs.vigia.model.Usuario;
import com.uscs.vigia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponse cadastrar(CadastroRequest cadastroRequest) {
        Usuario usuario = new Usuario();
        usuario.setNome(cadastroRequest.nome());
        usuario.setEmail(cadastroRequest.email());
        usuario.setTelefone(cadastroRequest.telefone());
        usuario.setDataNascimento(cadastroRequest.dataNascimento());
        usuario.setSenhaHash(passwordEncoder.encode(cadastroRequest.senha()));

        Usuario salvo = usuarioRepository.save(usuario);
        return toResponse(salvo);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone(), usuario.getDataNascimento(), usuario.getAvatar());
    }
}
