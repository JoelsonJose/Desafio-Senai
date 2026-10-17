package com.senai.backend.controller;

import com.senai.backend.dto.request.ClubeCreateRequest;
import com.senai.backend.dto.response.ClubeDetailResponse;
import com.senai.backend.dto.response.ClubeResponse;
import com.senai.backend.service.ClubeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import com.senai.backend.dto.request.LineupRequest;
import com.senai.backend.dto.response.LineupResponse;
import com.senai.backend.service.PartidaService;

@RestController
@RequestMapping("/api/v1/clubs")
public class ClubeController {

    private final ClubeService clubeService;
    private final PartidaService partidaService;

    public ClubeController(ClubeService clubeService, PartidaService partidaService) {
        this.clubeService = clubeService;
        this.partidaService = partidaService;
    }

    @PostMapping
    public ResponseEntity<ClubeResponse> criar(@Valid @RequestBody ClubeCreateRequest request) {
        ClubeResponse novoClube = clubeService.criar(request);
        URI location = URI.create("/api/v1/clubs/" + novoClube.getId());
        return ResponseEntity.created(location).body(novoClube);
    }

    @GetMapping
    public ResponseEntity<List<ClubeResponse>> listar(@RequestParam(required = false) String sort) {
        List<ClubeResponse> clubes = clubeService.listarTodos(sort);
        return ResponseEntity.ok(clubes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubeDetailResponse> buscarPorId(@PathVariable Integer id) {
        ClubeDetailResponse clube = clubeService.buscarDetalhePorId(id);
        return ResponseEntity.ok(clube);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        clubeService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/lineup")
    public ResponseEntity<LineupResponse> definirEscalacao(
            @PathVariable Integer id,
            @Valid @RequestBody LineupRequest request
    ) {
        LineupResponse response = partidaService.definirEscalacao(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/lineup")
    public ResponseEntity<LineupResponse> buscarEscalacao(@PathVariable Integer id) {
        LineupResponse response = partidaService.buscarEscalacao(id);
        return ResponseEntity.ok(response);
    }
}