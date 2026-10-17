package com.senai.backend.controller;

import com.senai.backend.dto.request.CondicaoRequest;
import com.senai.backend.dto.request.JogadorCreateRequest;
import com.senai.backend.dto.request.JogadorUpdateRequest;
import com.senai.backend.dto.response.JogadorResponse;
import com.senai.backend.dto.response.PaginatedResponse;
import com.senai.backend.service.JogadorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/players")
public class JogadorController {

    private final JogadorService jogadorService;

    public JogadorController(JogadorService jogadorService) {
        this.jogadorService = jogadorService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<JogadorResponse>> listar(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "position", required = false) String position,
            @RequestParam(name = "condition", required = false) String condition,
            @RequestParam(name = "free", required = false) Boolean free,
            @RequestParam(name = "club_id", required = false) Integer clubId,
            @RequestParam(name = "min_age", required = false) Integer minAge,
            @RequestParam(name = "max_age", required = false) Integer maxAge,
            @RequestParam(name = "min_value", required = false) BigDecimal minValue,
            @RequestParam(name = "max_value", required = false) BigDecimal maxValue,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        if (page < 1) {
            throw new IllegalArgumentException("O parâmetro page deve ser maior ou igual a 1.");
        }
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("O parâmetro limit deve estar entre 1 e 100.");
        }

        PaginatedResponse<JogadorResponse> response = jogadorService.listarPaginado(
                search, position, condition, free, clubId,
                minAge, maxAge, minValue, maxValue, sort, page, limit
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JogadorResponse> buscarPorId(@PathVariable Integer id) {
        JogadorResponse response = jogadorService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<JogadorResponse>> listarPorClube(@PathVariable Integer clubId) {
        List<JogadorResponse> response = jogadorService.listarPorClube(clubId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<JogadorResponse> criar(@Valid @RequestBody JogadorCreateRequest request) {
        JogadorResponse response = jogadorService.criar(request);
        URI location = URI.create("/api/v1/players/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JogadorResponse> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody JogadorUpdateRequest request
    ) {
        JogadorResponse response = jogadorService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/condition")
    public ResponseEntity<JogadorResponse> atualizarCondicao(
            @PathVariable Integer id,
            @Valid @RequestBody CondicaoRequest request
    ) {
        JogadorResponse response = jogadorService.atualizarCondicao(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        jogadorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}