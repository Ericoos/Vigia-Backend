package com.uscs.vigia.repository;

import com.uscs.vigia.model.Bairro;
import com.uscs.vigia.model.TipoBairro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BairroRepository extends JpaRepository<Bairro, TipoBairro> {
}
