package com.senai.backend.repository;

import com.senai.backend.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Integer>, JpaSpecificationExecutor<Transacao> {
    List<Transacao> findByClubeIdOrderByCriadoEmDescIdDesc(Integer clubeId);
}