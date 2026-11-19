package com.uscs.vigia.repository;

import com.uscs.vigia.model.Bairro;
import com.uscs.vigia.model.StatusReporte;
import com.uscs.vigia.model.TipoBairro;
import com.uscs.vigia.model.TipoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uscs.vigia.model.Reporte;

import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    List<Reporte> findAllByStatus(StatusReporte statusReporte);

    List<Reporte> findAllByBairroAndStatus(Bairro bairro, StatusReporte status);

    List<Reporte> findAllByStatusAndCategoria_TipoCategoria(StatusReporte status, TipoCategoria tipoCategoria);

    List<Reporte> findAllByStatusAndBairro_TipoBairro(StatusReporte status, TipoBairro tipoBairro);

    List<Reporte> findAllByStatusAndCategoria_TipoCategoriaAndBairro_TipoBairro(StatusReporte status, TipoCategoria tipoCategoria, TipoBairro tipoBairro);

    List<Reporte> findAllByUsuario_IdAndStatusNotOrderByDataReporteDesc(Long idUsuario, StatusReporte status);
}
