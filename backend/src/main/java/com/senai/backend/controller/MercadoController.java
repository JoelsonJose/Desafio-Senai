package com.senai.backend.controller;

import com.senai.backend.model.Transacao;
import com.senai.backend.service.MercadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MercadoController {

    private final MercadoService mercadoService;

    public MercadoController(MercadoService mercadoService) {
        this.mercadoService = mercadoService;
    }

    @PostMapping("/players/{id}/buy")
    public ResponseEntity<Map<String, Object>> comprar(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body) {
        
        Integer clubeId = (Integer) body.get("club_id");
        if (body.get("club_id") instanceof Number) {
            clubeId = ((Number) body.get("club_id")).intValue();
        }
        
        String loyaltyUntilStr = (String) body.get("loyalty_until");
        LocalDateTime loyaltyUntil = LocalDateTime.parse(loyaltyUntilStr.substring(0, 19)); 

        Transacao transacao = mercadoService.comprarJogador(id, clubeId, loyaltyUntil);

        Map<String, Object> response = new HashMap<>();
        response.put("id", transacao.getId());
        response.put("club_id", transacao.getClube().getId());
        response.put("player_id", transacao.getJogador().getId());
        response.put("player_name", transacao.getNomeJogador());
        response.put("type", transacao.getTipo());
        response.put("value", transacao.getValor());
        response.put("fee", transacao.getTaxa());
        response.put("penalty", transacao.getMulta());
        response.put("profit", transacao.getLucro());
        response.put("cash_change", transacao.getVariacaoCaixa());
        response.put("created_at", transacao.getCriadoEm());
        response.put("club_cash", transacao.getClube().getCaixa());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/players/{id}/sell")
    public ResponseEntity<Map<String, Object>> vender(@PathVariable Integer id) {
        Transacao transacao = mercadoService.venderJogador(id);

        Map<String, Object> response = new HashMap<>();
        response.put("id", transacao.getId());
        response.put("club_id", transacao.getClube().getId());
        response.put("player_id", transacao.getJogador().getId());
        response.put("player_name", transacao.getNomeJogador());
        response.put("type", transacao.getTipo());
        response.put("value", transacao.getValor());
        response.put("fee", transacao.getTaxa()); 
        response.put("penalty", transacao.getMulta());
        response.put("profit", transacao.getLucro());
        response.put("cash_change", transacao.getVariacaoCaixa());
        response.put("created_at", transacao.getCriadoEm());
        response.put("club_cash", transacao.getClube().getCaixa());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/clubs/{id}/transactions")
    public ResponseEntity<Map<String, Object>> listarTransacoes(@PathVariable Integer id) {
        List<Transacao> transacoes = mercadoService.listarTransacoesClube(id);

        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;
        BigDecimal totalPenalties = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (Transacao t : transacoes) {
            if ("COMPRA".equals(t.getTipo())) {
                totalSpent = totalSpent.add(t.getValor().add(t.getTaxa()));
                totalFees = totalFees.add(t.getTaxa());
            } else if ("VENDA".equals(t.getTipo())) {
                totalReceived = totalReceived.add(t.getVariacaoCaixa());
                totalFees = totalFees.add(t.getTaxa()); 
                totalPenalties = totalPenalties.add(t.getMulta());
                if (t.getLucro() != null) {
                    totalProfit = totalProfit.add(t.getLucro());
                }
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("total_spent", totalSpent);
        summary.put("total_received", totalReceived);
        summary.put("total_fees", totalFees);
        summary.put("total_penalties", totalPenalties);
        summary.put("total_profit", totalProfit);

        Map<String, Object> response = new HashMap<>();
        response.put("data", transacoes);
        response.put("summary", summary);

        return ResponseEntity.ok(response);
    }
}