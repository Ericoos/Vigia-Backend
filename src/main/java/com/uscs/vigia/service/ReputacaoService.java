package com.uscs.vigia.service;

import com.uscs.vigia.model.Usuario;
import com.uscs.vigia.repository.ReporteRepository;
import com.uscs.vigia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReputacaoService {

    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;

    public void registrarDenunciaRecebida(Usuario autor) {
        int total = autor.getTotalDenunciasRecebidas() == null ? 0 : autor.getTotalDenunciasRecebidas();
        autor.setTotalDenunciasRecebidas(total + 1);
        recalcular(autor);
    }

    public void recalcular(Usuario usuario) {
        long reportes = reporteRepository.countByUsuario_Id(usuario.getId());
        int denuncias = usuario.getTotalDenunciasRecebidas() == null ? 0 : usuario.getTotalDenunciasRecebidas();

        double avaliacao = 5.0 * (reportes + 1) / (reportes + 1 + denuncias);
        usuario.setAvaliacao(Math.round(avaliacao * 10) / 10.0);
        usuarioRepository.save(usuario);
    }
}
