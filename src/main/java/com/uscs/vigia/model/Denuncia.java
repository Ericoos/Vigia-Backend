package com.uscs.vigia.model;

import com.uscs.vigia.enums.StatusDenuncia;
import com.uscs.vigia.enums.MotivoDenuncia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDenuncia;

    @ManyToOne
    @JoinColumn(name = "usuario_denunciante_id")
    private Usuario usuarioDenunciante;

    @ManyToOne
    @JoinColumn(name = "reporte_id")
    private Reporte reporteAlvo;

    @Enumerated(EnumType.STRING)
    private MotivoDenuncia motivo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private OffsetDateTime dataDenuncia;

    @Enumerated(EnumType.STRING)
    private StatusDenuncia statusDenuncia;
}
