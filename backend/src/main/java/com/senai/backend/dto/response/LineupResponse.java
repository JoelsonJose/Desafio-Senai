package com.senai.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class LineupResponse {

    @JsonProperty("club_id")
    private Integer clubId;

    @JsonProperty("formation")
    private String formation;

    @JsonProperty("complete")
    private Boolean complete;

    @JsonProperty("players")
    private List<JogadorResponse> players;

    public LineupResponse() {
    }

    public LineupResponse(Integer clubId, String formation, Boolean complete, List<JogadorResponse> players) {
        this.clubId = clubId;
        this.formation = formation;
        this.complete = complete;
        this.players = players;
    }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }

    public String getFormation() { return formation; }
    public void setFormation(String formation) { this.formation = formation; }

    public Boolean getComplete() { return complete; }
    public void setComplete(Boolean complete) { this.complete = complete; }

    public List<JogadorResponse> getPlayers() { return players; }
    public void setPlayers(List<JogadorResponse> players) { this.players = players; }
}
