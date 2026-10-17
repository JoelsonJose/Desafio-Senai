package com.senai.backend.repository;

import com.senai.backend.model.Jogador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JogadorRepository extends JpaRepository<Jogador, Integer>, JpaSpecificationExecutor<Jogador> {

    Optional<Jogador> findByIdAndExcluidoEmIsNull(Integer id);

    List<Jogador> findByClubeId(Integer clubeId);

    List<Jogador> findByClubeIdAndExcluidoEmIsNull(Integer clubeId);

    List<Jogador> findAllByExcluidoEmIsNull();
}