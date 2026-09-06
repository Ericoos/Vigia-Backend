package com.uscs.vigia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uscs.vigia.model.Reporte;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
}