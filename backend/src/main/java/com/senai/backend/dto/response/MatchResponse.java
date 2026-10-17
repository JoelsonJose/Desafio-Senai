package com.senai.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchResponse {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("played_at")
    private String playedAt;

    @JsonProperty("home")
    private MatchClubResponse home;

    @JsonProperty("away")
    private MatchClubResponse away;

    @JsonProperty("result")
    private String result;

    @JsonProperty("duels")
    private List<MatchDuelResponse> duels;

    public MatchResponse() {
    }

    public MatchResponse(Integer id, String playedAt, MatchClubResponse home, MatchClubResponse away, String result, List<MatchDuelResponse> duels) {
        this.id = id;
        this.playedAt = playedAt;
        this.home = home;
        this.away = away;
        this.result = result;
        this.duels = duels;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getPlayedAt() { return playedAt; }
    public void setPlayedAt(String playedAt) { this.playedAt = playedAt; }

    public MatchClubResponse getHome() { return home; }
    public void setHome(MatchClubResponse home) { this.home = home; }

    public MatchClubResponse getAway() { return away; }
    public void setAway(MatchClubResponse away) { this.away = away; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public List<MatchDuelResponse> getDuels() { return duels; }
    public void setDuels(List<MatchDuelResponse> duels) { this.duels = duels; }
}
