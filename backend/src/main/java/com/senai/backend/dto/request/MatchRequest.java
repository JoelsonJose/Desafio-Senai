package com.senai.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class MatchRequest {

    @NotNull(message = "O campo home_club_id é obrigatório.")
    @JsonProperty("home_club_id")
    private Integer homeClubId;

    @NotNull(message = "O campo away_club_id é obrigatório.")
    @JsonProperty("away_club_id")
    private Integer awayClubId;

    public void validate() {
        if (this.homeClubId == null || this.homeClubId <= 0) {
            throw new IllegalArgumentException("O home_club_id deve ser um inteiro positivo.");
        }
        if (this.awayClubId == null || this.awayClubId <= 0) {
            throw new IllegalArgumentException("O away_club_id deve ser um inteiro positivo.");
        }
        if (this.homeClubId.equals(this.awayClubId)) {
            throw new IllegalArgumentException("O clube mandante e o clube visitante devem ser diferentes.");
        }
    }

    public Integer getHomeClubId() { return homeClubId; }
    public void setHomeClubId(Integer homeClubId) { this.homeClubId = homeClubId; }

    public Integer getAwayClubId() { return awayClubId; }
    public void setAwayClubId(Integer awayClubId) { this.awayClubId = awayClubId; }
}
