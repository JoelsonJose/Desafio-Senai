package com.senai.backend.controller;

import com.senai.backend.dto.request.CompraRequest;
import com.senai.backend.dto.response.CompraResponse;
import com.senai.backend.dto.response.TransacoesListResponse;
import com.senai.backend.dto.response.VendaResponse;
import com.senai.backend.service.MercadoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class MercadoController {

    private final MercadoService mercadoService;

    public MercadoController(MercadoService mercadoService) {
        this.mercadoService = mercadoService;
    }

    @PostMapping("/players/{id}/buy")
    public ResponseEntity<CompraResponse> comprar(
            @PathVariable Integer id,
            @Valid @RequestBody CompraRequest request
    ) {
        CompraResponse response = mercadoService.comprarJogador(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/players/{id}/sell")
    public ResponseEntity<VendaResponse> vender(@PathVariable Integer id) {
        VendaResponse response = mercadoService.venderJogador(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clubs/{id}/transactions")
    public ResponseEntity<TransacoesListResponse> listarTransacoes(
            @PathVariable Integer id,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "player_id", required = false) Integer playerId
    ) {
        TransacoesListResponse response = mercadoService.listarTransacoesClube(id, type, playerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/clubs/{id}/transactions/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportarTransacoesCsv(
            @PathVariable Integer id,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "player_id", required = false) Integer playerId
    ) {
        byte[] csv = mercadoService.exportarTransacoesCsv(id, type, playerId);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(csv);
    }
}