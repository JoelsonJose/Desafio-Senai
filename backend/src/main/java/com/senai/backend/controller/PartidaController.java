package com.senai.backend.controller;

import com.senai.backend.dto.request.MatchRequest;
import com.senai.backend.dto.response.MatchResponse;
import com.senai.backend.dto.response.StandingResponse;
import com.senai.backend.service.PartidaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PartidaController {

    private final PartidaService partidaService;

    public PartidaController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    @PostMapping("/matches")
    public ResponseEntity<MatchResponse> jogarPartida(@Valid @RequestBody MatchRequest request) {
        MatchResponse response = partidaService.jogarPartida(request);
        URI location = URI.create("/api/v1/matches/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/matches")
    public ResponseEntity<List<MatchResponse>> listarPartidas(
            @RequestParam(name = "club_id", required = false) Integer clubId
    ) {
        List<MatchResponse> response = partidaService.listarPartidas(clubId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/matches/{id}")
    public ResponseEntity<MatchResponse> buscarPorId(@PathVariable Integer id) {
        MatchResponse response = partidaService.buscarPartidaPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/standings")
    public ResponseEntity<List<StandingResponse>> obterClassificacao() {
        List<StandingResponse> response = partidaService.obterClassificacao();
        return ResponseEntity.ok(response);
    }
}
