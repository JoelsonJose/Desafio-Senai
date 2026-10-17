package com.senai.backend.controller;

import com.senai.backend.model.Clube;
import com.senai.backend.model.Jogador;
import com.senai.backend.service.ClubeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clubs")
public class ClubeController {

    private final ClubeService clubeService;

    public ClubeController(ClubeService clubeService) {
        this.clubeService = clubeService;
    }

    @PostMapping
    public ResponseEntity<Clube> criar(@RequestBody Clube clube) {
        Clube novoClube = clubeService.salvar(clube);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoClube);
    }

    @GetMapping
    public ResponseEntity<List<Clube>> listar(@RequestParam(required = false) String sort) {
        List<Clube> clubes = clubeService.listarTodos(sort);
        return ResponseEntity.ok(clubes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        clubeService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarPorId(@PathVariable Integer id) {
        Clube clube = clubeService.buscarPorId(id);
        BigDecimal valorElenco = clubeService.calcularValorElenco(id);
        BigDecimal patrimonio = clubeService.calcularPatrimonio(clube);
        List<Jogador> elenco = clubeService.obterElencoOrdenado(id);

        // Montar resposta detalhada exigida pela RN11
        Map<String, Object> response = new HashMap<>();
        response.put("id", clube.getId());
        response.put("name", clube.getNome());
        response.put("acronym", clube.getSigla());
        response.put("cash", clube.getCaixa());
        response.put("formation", clube.getFormacao());
        response.put("created_at", clube.getCriado_em());
        response.put("squad_value", valorElenco);
        response.put("net_worth", patrimonio);
        response.put("squad", elenco);

        return ResponseEntity.ok(response);
    }
}