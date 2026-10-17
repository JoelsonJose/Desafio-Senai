package com.senai.backend.repository;

import com.senai.backend.model.Clube;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubeRepository extends JpaRepository<Clube, Integer> {

    boolean existsBySigla(String sigla);

    boolean existsByNomeNormalizado(String nomeNormalizado);

    Optional<Clube> findBySigla(String sigla);
}