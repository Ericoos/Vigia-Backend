package com.uscs.vigia.service;

import com.uscs.vigia.dto.DenunciaRequest;
import com.uscs.vigia.dto.DenunciaResponse;
import com.uscs.vigia.enums.StatusDenuncia;
import com.uscs.vigia.enums.StatusReporte;
import com.uscs.vigia.exception.AcessoNegadoException;
import com.uscs.vigia.exception.ConflitoException;
import com.uscs.vigia.exception.RecursoNaoEncontradoException;
import com.uscs.vigia.model.Denuncia;
import com.uscs.vigia.model.Reporte;
import com.uscs.vigia.model.Usuario;
import com.uscs.vigia.repository.DenunciaRepository;
import com.uscs.vigia.repository.ReporteRepository;
import com.uscs.vigia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReporteRepository reporteRepository;

    @Transactional
    public DenunciaResponse denunciar(Long idReporte, DenunciaRequest request, Long idUsuario) {
        Usuario usuarioDenunciante = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + idUsuario));

        Reporte reporteAlvo = reporteRepository.findById(idReporte)
                .filter(reporte -> reporte.getStatus() == StatusReporte.PUBLICADO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reporte não encontrado: " + idReporte));

        if (reporteAlvo.getUsuario() != null && reporteAlvo.getUsuario().getId().equals(idUsuario)) {
            throw new AcessoNegadoException("Você não pode denunciar o próprio reporte");
        }

        if (denunciaRepository.existsByUsuarioDenunciante_IdAndReporteAlvo_IdReporte(idUsuario, idReporte)) {
            throw new ConflitoException("Você já denunciou esse reporte");
        }

        Denuncia denuncia = new Denuncia();
        denuncia.setUsuarioDenunciante(usuarioDenunciante);
        denuncia.setReporteAlvo(reporteAlvo);
        denuncia.setMotivo(request.motivo());
        denuncia.setDescricao(request.descricao());
        denuncia.setDataDenuncia(OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")));
        denuncia.setStatusDenuncia(StatusDenuncia.PENDENTE);

        Denuncia denunciaSalva = denunciaRepository.save(denuncia);
        return toResponse(denunciaSalva);
    }

    private DenunciaResponse toResponse(Denuncia denuncia) {
        return new DenunciaResponse(
                denuncia.getIdDenuncia(),
                denuncia.getReporteAlvo().getIdReporte(),
                denuncia.getMotivo(),
                denuncia.getDescricao(),
                denuncia.getDataDenuncia(),
                denuncia.getStatusDenuncia()
        );
    }
}
