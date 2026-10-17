package com.senai.backend.util;

import com.senai.backend.dto.response.ClubeDetailResponse;
import com.senai.backend.dto.response.ClubeResponse;
import com.senai.backend.dto.response.JogadorResponse;
import com.senai.backend.model.Clube;

import java.math.BigDecimal;
import java.util.List;

public final class ClubeMapper {

    private ClubeMapper() {
    }

    public static ClubeResponse toResponse(Clube clube, BigDecimal squadValue, BigDecimal netWorth) {
        if (clube == null) {
            return null;
        }

        return new ClubeResponse(
                clube.getId(),
                clube.getNome(),
                clube.getSigla(),
                clube.getCaixa(),
                squadValue != null ? squadValue : BigDecimal.ZERO,
                netWorth != null ? netWorth : clube.getCaixa()
        );
    }

    public static ClubeDetailResponse toDetailResponse(
            Clube clube,
            BigDecimal squadValue,
            BigDecimal netWorth,
            List<JogadorResponse> squad
    ) {
        if (clube == null) {
            return null;
        }

        return new ClubeDetailResponse(
                clube.getId(),
                clube.getNome(),
                clube.getSigla(),
                clube.getCaixa(),
                squadValue != null ? squadValue : BigDecimal.ZERO,
                netWorth != null ? netWorth : clube.getCaixa(),
                squad
        );
    }
}
