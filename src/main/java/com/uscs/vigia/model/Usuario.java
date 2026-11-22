package com.uscs.vigia.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Usuario extends UsuarioBase {

    private String telefone;
    private Double avaliacao;
    private String avatar;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Integer totalDenunciasRecebidas;
    private Boolean telefoneConfirmado;
    private String codigoVerificacao;
    private LocalDateTime codigoExpiraEm;
    private Integer tentativasCodigo;

}
