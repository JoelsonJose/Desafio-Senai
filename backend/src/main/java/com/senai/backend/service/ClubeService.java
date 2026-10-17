package com.senai.backend.service;

import com.senai.backend.exception.BusinessException;
import com.senai.backend.exception.ResourceNotFoundException;
import com.senai.backend.model.Clube;
import com.senai.backend.model.Jogador;
import com.senai.backend.repository.ClubeRepository;
import com.senai.backend.repository.JogadorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;

@Service
public class ClubeService {

    private final ClubeRepository clubeRepository;
    private final JogadorRepository jogadorRepository;

    public ClubeService(ClubeRepository clubeRepository, JogadorRepository jogadorRepository) {
        this.clubeRepository = clubeRepository;
        this.jogadorRepository = jogadorRepository;
    }

    public List<Clube> listarTodos(String sort) {
        if ("net_worth_asc".equals(sort) || "net_worth_desc".equals(sort)) {
            List<Clube> clubes = clubeRepository.findAll();
            clubes.sort((c1, c2) -> {
                BigDecimal p1 = calcularPatrimonio(c1);
                BigDecimal p2 = calcularPatrimonio(c2);
                int cmp = p1.compareTo(p2);
                if (cmp == 0) {
                    cmp = c1.getNome().compareToIgnoreCase(c2.getNome());
                }
                if (cmp == 0) {
                    cmp = c1.getId().compareTo(c2.getId());
                }
                return "net_worth_desc".equals(sort) ? -cmp : cmp;
            });
            return clubes;
        } else {
            return clubeRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
        }
    }

    public Clube buscarPorId(Integer id) {
        return clubeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clube não encontrado com o ID: " + id));
    }

    public Clube salvar(Clube clube) {
        long totalClubes = clubeRepository.count();
        if (totalClubes >= 20) {
            throw new BusinessException("O limite máximo de 20 clubes na liga já foi atingido.");
        }

        if (clube.getNome() != null) {
            clube.setNome(clube.getNome().replaceAll("\\s+", " ").trim());
        }

        if (clube.getSigla() != null) {
            clube.setSigla(clube.getSigla().trim().toUpperCase());
        }

        if (clubeRepository.existsBySigla(clube.getSigla())) {
            throw new BusinessException("Já existe um clube cadastrado com esta sigla.");
        }

        clube.setCaixa(new BigDecimal("300.00"));

        try {
            return clubeRepository.save(clube);
        } catch (Exception e) {
            throw new BusinessException("Já existe um clube cadastrado com este nome.");
        }
    }

    public void deletar(Integer id) {
        Clube clube = buscarPorId(id); 
        clubeRepository.delete(clube);
    }

    public BigDecimal calcularValorElenco(Integer clubeId) {
        List<Jogador> elenco = jogadorRepository.findByClubeId(clubeId);
        BigDecimal soma = BigDecimal.ZERO;
        for (Jogador j : elenco) {
            if (j.getValorMercado() != null) {
                soma = soma.add(j.getValorMercado());
            }
        }
        return soma;
    }

    public BigDecimal calcularPatrimonio(Clube clube) {
        BigDecimal valorElenco = calcularValorElenco(clube.getId());
        return clube.getCaixa().add(valorElenco);
    }

    public List<Jogador> obterElencoOrdenado(Integer clubeId) {
        List<Jogador> elenco = jogadorRepository.findByClubeId(clubeId);
        
        Map<String, Integer> ordemPosicao = Map.of(
            "GOL", 1,
            "ZAG", 2,
            "LAT", 3,
            "MEI", 4,
            "ATA", 5
        );

        elenco.sort((j1, j2) -> {
            int p1 = ordemPosicao.getOrDefault(j1.getPosicao(), 99);
            int p2 = ordemPosicao.getOrDefault(j2.getPosicao(), 99);
            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
            int cmpNome = j1.getNome().compareToIgnoreCase(j2.getNome());
            if (cmpNome != 0) {
                return cmpNome;
            }
            return Integer.compare(j1.getId(), j2.getId());
        });

        return elenco;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String normalized = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").replaceAll("\\s+", " ").toLowerCase();
    }
}