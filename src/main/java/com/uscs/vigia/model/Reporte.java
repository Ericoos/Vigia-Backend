package com.uscs.vigia.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Reporte {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idReporte;

	private String titulo;
	private String descricao;
	private double latitude;
	private double longitude;
	private String endereco;
	private Integer avaliacaoGravidade;

	@Enumerated(EnumType.STRING)
	private StatusReporte status;

	private OffsetDateTime dataReporte;

	@ManyToOne
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@ManyToOne
	@JoinColumn(name = "categoria_id")
	private Categoria categoria;

	@ManyToOne
	@JoinColumn(name = "subcategoria_id")
	private Subcategoria subcategoria;

	@ManyToOne
	@JoinColumn(name = "bairro_id")
	private Bairro bairro;
}