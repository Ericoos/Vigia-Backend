package com.uscs.vigia.repository;

import com.uscs.vigia.model.Bairro;
import com.uscs.vigia.enums.StatusReporte;
import com.uscs.vigia.enums.TipoBairro;
import com.uscs.vigia.enums.TipoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uscs.vigia.model.Reporte;

import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    List<Reporte> findAllByStatusIn(List<StatusReporte> status);

    List<Reporte> findAllByBairroAndStatusIn(Bairro bairro, List<StatusReporte> status);

    List<Reporte> findAllByStatusInAndCategoria_TipoCategoria(List<StatusReporte> status, TipoCategoria tipoCategoria);

    List<Reporte> findAllByStatusInAndBairro_TipoBairro(List<StatusReporte> status, TipoBairro tipoBairro);

    List<Reporte> findAllByStatusInAndCategoria_TipoCategoriaAndBairro_TipoBairro(List<StatusReporte> status, TipoCategoria tipoCategoria, TipoBairro tipoBairro);

    List<Reporte> findAllByUsuario_IdAndStatusNotOrderByDataReporteDesc(Long idUsuario, StatusReporte status);

    long countByUsuario_Id(Long idUsuario);
}
