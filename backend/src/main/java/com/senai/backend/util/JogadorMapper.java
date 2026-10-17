package com.senai.backend.util;

import com.senai.backend.dto.response.ContratoInfoResponse;
import com.senai.backend.dto.response.JogadorResponse;
import com.senai.backend.model.Contrato;
import com.senai.backend.model.Jogador;

import java.time.Duration;
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
            LocalDateTime agora = LocalDateTime.now();
            long horasRestantes = Duration.between(agora, contratoAtivo.getFidelidadeAte()).toHours();
            long diasRestantes = 0;
            if (horasRestantes > 0) {
                diasRestantes = (long) Math.ceil((double) horasRestantes / 24.0);
            }

            int percentualMulta = 0;
            if (diasRestantes >= 1 && diasRestantes <= 30) {
                percentualMulta = 5;
            } else if (diasRestantes >= 31 && diasRestantes <= 180) {
                percentualMulta = 10;
            } else if (diasRestantes >= 181) {
                percentualMulta = 20;
            }

            String loyaltyUntilStr = DateTimeUtils.formatResponse(contratoAtivo.getFidelidadeAte());

            ContratoInfoResponse contratoInfo = new ContratoInfoResponse(
                    contratoAtivo.getValorCompra(),
                    loyaltyUntilStr,
                    diasRestantes,
                    percentualMulta
            );
            resp.setContract(contratoInfo);
        }

        return resp;
    }
}
