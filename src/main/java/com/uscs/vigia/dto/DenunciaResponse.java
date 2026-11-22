package com.uscs.vigia.dto;

import com.uscs.vigia.enums.MotivoDenuncia;
import com.uscs.vigia.enums.StatusDenuncia;

import java.time.OffsetDateTime;

public record DenunciaResponse(Long idDenuncia, Long idReporte, MotivoDenuncia motivo, String descricao, OffsetDateTime dataDenuncia, StatusDenuncia statusDenuncia) {
}
