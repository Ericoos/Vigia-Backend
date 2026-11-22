package com.uscs.vigia.repository;

import com.uscs.vigia.model.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    boolean existsByUsuarioDenunciante_IdAndReporteAlvo_IdReporte(Long idUsuarioDenunciante, Long idReporteAlvo);

    long countByReporteAlvo_IdReporte(Long idReporte);
}
