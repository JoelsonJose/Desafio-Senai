package com.senai.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StandingResponse {

    @JsonProperty("position")
    private Integer position;

    @JsonProperty("club_id")
    private Integer clubId;

    @JsonProperty("club_name")
    private String clubName;

    @JsonProperty("played")
    private Integer played;

    @JsonProperty("wins")
    private Integer wins;

    @JsonProperty("draws")
    private Integer draws;

    @JsonProperty("losses")
    private Integer losses;

    @JsonProperty("goals_for")
    private Integer goalsFor;

    @JsonProperty("goals_against")
    private Integer goalsAgainst;

    @JsonProperty("goal_difference")
    private Integer goalDifference;

    @JsonProperty("points")
    private Integer points;

    public StandingResponse() {
    }

    public StandingResponse(Integer position, Integer clubId, String clubName, Integer played, Integer wins, Integer draws, Integer losses, Integer goalsFor, Integer goalsAgainst, Integer goalDifference, Integer points) {
        this.position = position;
        this.clubId = clubId;
        this.clubName = clubName;
        this.played = played;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.goalDifference = goalDifference;
        this.points = points;
    }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }

    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }

    public Integer getPlayed() { return played; }
    public void setPlayed(Integer played) { this.played = played; }

    public Integer getWins() { return wins; }
    public void setWins(Integer wins) { this.wins = wins; }

    public Integer getDraws() { return draws; }
    public void setDraws(Integer draws) { this.draws = draws; }

    public Integer getLosses() { return losses; }
    public void setLosses(Integer losses) { this.losses = losses; }

    public Integer getGoalsFor() { return goalsFor; }
    public void setGoalsFor(Integer goalsFor) { this.goalsFor = goalsFor; }

    public Integer getGoalsAgainst() { return goalsAgainst; }
    public void setGoalsAgainst(Integer goalsAgainst) { this.goalsAgainst = goalsAgainst; }

    public Integer getGoalDifference() { return goalDifference; }
    public void setGoalDifference(Integer goalDifference) { this.goalDifference = goalDifference; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
}
