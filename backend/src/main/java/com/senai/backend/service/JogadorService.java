package com.senai.backend.service;

import com.senai.backend.exception.ResourceNotFoundException;
import com.senai.backend.model.Jogador;
import com.senai.backend.repository.JogadorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JogadorService {

    private final JogadorRepository jogadorRepository;

    public JogadorService(JogadorRepository jogadorRepository) {
        this.jogadorRepository = jogadorRepository;
    }

    public List<Jogador> listarTodos() {
        return jogadorRepository.findAll();
    }

    public Jogador buscarPorId(Integer id) {
        return jogadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado com o ID: " + id));
    }

    public List<Jogador> listarPorClube(Integer clubeId) {
        return jogadorRepository.findByClubeId(clubeId);
    }

    public Jogador salvar(Jogador jogador) {
        return jogadorRepository.save(jogador);
    }

    public Jogador atualizar(Integer id, Jogador jogadorDados) {
        Jogador jogadorExistente = buscarPorId(id);

        jogadorExistente.setNome(jogadorDados.getNome());
        jogadorExistente.setPosicao(jogadorDados.getPosicao());
        jogadorExistente.setIdade(jogadorDados.getIdade());
        jogadorExistente.setPotencial(jogadorDados.getPotencial());
        jogadorExistente.setCondicao(jogadorDados.getCondicao());
        jogadorExistente.setVelocidade(jogadorDados.getVelocidade());
        jogadorExistente.setFinalizacao(jogadorDados.getFinalizacao());
        jogadorExistente.setPasse(jogadorDados.getPasse());
        jogadorExistente.setDrible(jogadorDados.getDrible());
        jogadorExistente.setDefesa(jogadorDados.getDefesa());
        jogadorExistente.setFisico(jogadorDados.getFisico());
        jogadorExistente.setAgilidade(jogadorDados.getAgilidade());
        jogadorExistente.setClube(jogadorDados.getClube());

        return jogadorRepository.save(jogadorExistente);
    }

    public void deletar(Integer id) {
        Jogador jogador = buscarPorId(id);
        jogador.setExcluidoEm(LocalDateTime.now());
        jogadorRepository.save(jogador);
    }
}