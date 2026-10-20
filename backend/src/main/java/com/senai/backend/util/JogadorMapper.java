package com.senai.backend.util;

import com.senai.backend.dto.response.ContratoInfoResponse;
import com.senai.backend.dto.response.JogadorResponse;
import com.senai.backend.model.Contrato;
import com.senai.backend.model.Jogador;

import java.time.LocalDateTime;

public final class JogadorMapper {

    private JogadorMapper() {
    }

    public static JogadorResponse toResponse(Jogador jogador, Contrato contratoAtivo) {
        if (jogador == null) {
            return null;
        }

        JogadorResponse resp = new JogadorResponse();
        resp.setId(jogador.getId());
        resp.setName(jogador.getNome());
        resp.setPosition(jogador.getPosicao());
        resp.setAgility(jogador.getAgilidade());
        resp.setAge(jogador.getIdade());
        resp.setCondition(jogador.getCondicao());
        resp.setBaseValue(jogador.getValorBase());
        resp.setMarketValue(jogador.getValorMercado());

        resp.setPace(jogador.getVelocidade());
        resp.setShooting(jogador.getFinalizacao());
        resp.setPassing(jogador.getPasse());
        resp.setDribbling(jogador.getDrible());
        resp.setDefending(jogador.getDefesa());
        resp.setPhysical(jogador.getFisico());
        resp.setPotential(jogador.getPotencial());
        resp.setOverall(jogador.getOverall());

        if (jogador.getClube() != null) {
            resp.setClubId(jogador.getClube().getId());
        }

        if (contratoAtivo != null) {
            LoyaltyCalculator.Calculation fidelidade = LoyaltyCalculator.calculate(
                    LocalDateTime.now(), contratoAtivo.getFidelidadeAte()
            );

            String loyaltyUntilStr = DateTimeUtils.formatResponse(contratoAtivo.getFidelidadeAte());

            ContratoInfoResponse contratoInfo = new ContratoInfoResponse(
                    contratoAtivo.getValorCompra(),
                    loyaltyUntilStr,
                    fidelidade.remainingDays(),
                    fidelidade.penaltyPercentage()
            );
            resp.setContract(contratoInfo);
        }

        return resp;
    }
}
