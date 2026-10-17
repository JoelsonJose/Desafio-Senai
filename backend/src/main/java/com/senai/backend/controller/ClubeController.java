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

@RestController
@RequestMapping("/api/v1/clubs")
public class ClubeController {

    private final ClubeService clubeService;

    public ClubeController(ClubeService clubeService) {
        this.clubeService = clubeService;
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
}