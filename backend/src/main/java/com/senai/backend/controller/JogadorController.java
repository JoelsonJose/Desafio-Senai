package com.senai.backend.controller;

import com.senai.backend.model.Jogador;
import com.senai.backend.service.JogadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jogadores")
public class JogadorController {

    private final JogadorService jogadorService;

    public JogadorController(JogadorService jogadorService) {
        this.jogadorService = jogadorService;
    }

    @GetMapping
    public ResponseEntity<List<Jogador>> listarTodos() {
        List<Jogador> jogadores = jogadorService.listarTodos();
        return ResponseEntity.ok(jogadores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Jogador> buscarPorId(@PathVariable Integer id) {
        Jogador jogador = jogadorService.buscarPorId(id);
        return ResponseEntity.ok(jogador);
    }

    @GetMapping("/clube/{clubeId}")
    public ResponseEntity<List<Jogador>> listarPorClube(@PathVariable Integer clubeId) {
        List<Jogador> jogadores = jogadorService.listarPorClube(clubeId);
        return ResponseEntity.ok(jogadores);
    }

    @PostMapping
    public ResponseEntity<Jogador> criar(@Valid @RequestBody Jogador jogador) {
        Jogador novoJogador = jogadorService.salvar(jogador);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoJogador);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Jogador> atualizar(@PathVariable Integer id, @Valid @RequestBody Jogador jogador) {
        Jogador jogadorAtualizado = jogadorService.atualizar(id, jogador);
        return ResponseEntity.ok(jogadorAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        jogadorService.deletar(id);
        return ResponseEntity.noContent().build();
    }   
}