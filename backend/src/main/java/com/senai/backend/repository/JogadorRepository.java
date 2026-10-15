package com.senai.backend.repository;

import com.senai.backend.model.Jogador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JogadorRepository extends JpaRepository<Jogador, Integer> {

    List<Jogador> findByClubeId(Integer clubeId);
}