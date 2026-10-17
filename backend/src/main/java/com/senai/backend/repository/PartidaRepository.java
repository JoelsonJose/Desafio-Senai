package com.senai.backend.repository;

import com.senai.backend.model.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Integer> {

    boolean existsByMandanteIdOrVisitanteId(Integer mandanteId, Integer visitanteId);

    List<Partida> findByMandanteIdOrVisitanteId(Integer mandanteId, Integer visitanteId);
}
