package com.senai.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.senai.backend.util.DateTimeUtils;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class CompraRequest {

    @NotNull(message = "O campo club_id é obrigatório.")
    @JsonProperty("club_id")
    private Integer clubId;

    @NotNull(message = "O campo loyalty_until é obrigatório.")
    @JsonProperty("loyalty_until")
    private String loyaltyUntil;

    public OffsetDateTime validateAndGetLoyaltyUntil() {
        if (this.clubId == null || this.clubId <= 0) {
            throw new IllegalArgumentException("O club_id deve ser um número inteiro positivo.");
        }

        OffsetDateTime parsedDate = DateTimeUtils.parseRequestDateTime(this.loyaltyUntil);
        OffsetDateTime agora = DateTimeUtils.now();

        if (!parsedDate.isAfter(agora)) {
            throw new IllegalArgumentException("A data de término da fidelidade deve ser posterior ao momento da compra.");
        }

        if (parsedDate.isAfter(agora.plusDays(1095))) {
            throw new IllegalArgumentException("A data de término da fidelidade não pode ultrapassar 1.095 dias a partir da compra.");
        }

        return parsedDate;
    }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }

    public String getLoyaltyUntil() { return loyaltyUntil; }
    public void setLoyaltyUntil(String loyaltyUntil) { this.loyaltyUntil = loyaltyUntil; }
}
