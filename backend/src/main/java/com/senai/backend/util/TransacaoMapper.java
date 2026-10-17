package com.senai.backend.util;

import com.senai.backend.dto.response.CompraResponse;
import com.senai.backend.dto.response.TransacaoResponse;
import com.senai.backend.dto.response.VendaResponse;
import com.senai.backend.model.Contrato;
import com.senai.backend.model.Transacao;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public final class TransacaoMapper {

    private TransacaoMapper() {
    }

    public static CompraResponse toCompraResponse(Transacao transacao, Contrato contrato, BigDecimal clubCash) {
        if (transacao == null) {
            return null;
        }

        CompraResponse resp = new CompraResponse();
        resp.setId(transacao.getId());
        resp.setClubId(transacao.getClube().getId());
        resp.setPlayerId(transacao.getJogador().getId());
        resp.setPlayerName(transacao.getNomeJogador());
        resp.setType(transacao.getTipo());
        resp.setValue(transacao.getValor());
        resp.setFee(transacao.getTaxa());
        resp.setPenalty(transacao.getMulta());
        resp.setProfit(transacao.getLucro());
        resp.setCashChange(transacao.getVariacaoCaixa());
        resp.setCreatedAt(DateTimeUtils.formatResponse(transacao.getCriadoEm()));
        resp.setClubCash(clubCash);

        if (contrato != null) {
            Map<String, Object> contratoMap = new HashMap<>();
            contratoMap.put("id", contrato.getId());
            contratoMap.put("purchase_value", contrato.getValorCompra());
            contratoMap.put("loyalty_until", DateTimeUtils.formatResponse(contrato.getFidelidadeAte()));
            contratoMap.put("status", contrato.getSituacao());
            resp.setContract(contratoMap);
        }

        return resp;
    }

    public static VendaResponse toVendaResponse(
            Transacao transacao,
            BigDecimal purchaseValue,
            long remainingDays,
            int penaltyRate,
            String contractStatus,
            BigDecimal clubCash
    ) {
        if (transacao == null) {
            return null;
        }

        VendaResponse resp = new VendaResponse();
        resp.setId(transacao.getId());
        resp.setClubId(transacao.getClube().getId());
        resp.setPlayerId(transacao.getJogador().getId());
        resp.setPlayerName(transacao.getNomeJogador());
        resp.setType(transacao.getTipo());
        resp.setValue(transacao.getValor());
        resp.setFee(transacao.getTaxa());
        resp.setPenalty(transacao.getMulta());
        resp.setProfit(transacao.getLucro());
        resp.setCashChange(transacao.getVariacaoCaixa());
        resp.setCreatedAt(DateTimeUtils.formatResponse(transacao.getCriadoEm()));
        resp.setPurchaseValue(purchaseValue);
        resp.setRemainingDays(remainingDays);
        resp.setPenaltyRate(penaltyRate);
        resp.setContractStatus(contractStatus);
        resp.setClubCash(clubCash);

        return resp;
    }

    public static TransacaoResponse toResponse(Transacao transacao) {
        if (transacao == null) {
            return null;
        }

        TransacaoResponse resp = new TransacaoResponse();
        resp.setId(transacao.getId());
        resp.setClubId(transacao.getClube().getId());
        resp.setPlayerId(transacao.getJogador().getId());
        resp.setPlayerName(transacao.getNomeJogador());
        resp.setType(transacao.getTipo());
        resp.setValue(transacao.getValor());
        resp.setFee(transacao.getTaxa());
        resp.setPenalty(transacao.getMulta());
        resp.setProfit(transacao.getLucro());
        resp.setCashChange(transacao.getVariacaoCaixa());
        resp.setCreatedAt(DateTimeUtils.formatResponse(transacao.getCriadoEm()));

        return resp;
    }
}
