package com.senai.backend.repository;

import com.senai.backend.model.DueloPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DueloPartidaRepository extends JpaRepository<DueloPartida, Integer> {

    List<DueloPartida> findByPartidaIdOrderByOrdemAsc(Integer partidaId);
}
