package com.uscs.vigia.repository;

import com.uscs.vigia.model.StatusReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uscs.vigia.model.Reporte;

import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    public List<Reporte> findAllByStatus(StatusReporte statusReporte);
}