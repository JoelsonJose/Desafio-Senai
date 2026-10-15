package com.senai.backend.repository;

import com.senai.backend.model.Clube;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubeRepository extends JpaRepository<Clube, Long> {

    boolean existsBySigla(String sigla);

    Optional<Clube> findBySigla(String sigla);
}