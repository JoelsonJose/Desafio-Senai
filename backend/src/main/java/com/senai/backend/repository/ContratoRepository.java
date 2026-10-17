package com.senai.backend.repository;

import com.senai.backend.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Integer> {
    Optional<Contrato> findByJogadorIdAndSituacao(Integer jogadorId, String situacao);
}