package com.senai.backend.repository;

import com.senai.backend.model.Escalacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalacaoRepository extends JpaRepository<Escalacao, Escalacao.EscalacaoId> {

    List<Escalacao> findByClubeId(Integer clubeId);

    void deleteByClubeId(Integer clubeId);

    void deleteByJogadorId(Integer jogadorId);

    boolean existsByJogadorId(Integer jogadorId);
}
