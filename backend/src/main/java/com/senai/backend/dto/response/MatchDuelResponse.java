package com.senai.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MatchDuelResponse {

    @JsonProperty("order")
    private Integer order;

    @JsonProperty("attacking_club_id")
    private Integer attackingClubId;

    @JsonProperty("attacker")
    private MatchDuelPlayerResponse attacker;

    @JsonProperty("defender")
    private MatchDuelPlayerResponse defender;

    @JsonProperty("goal")
    private Boolean goal;

    public MatchDuelResponse() {
    }

    public MatchDuelResponse(Integer order, Integer attackingClubId, MatchDuelPlayerResponse attacker, MatchDuelPlayerResponse defender, Boolean goal) {
        this.order = order;
        this.attackingClubId = attackingClubId;
        this.attacker = attacker;
        this.defender = defender;
        this.goal = goal;
    }

    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }

    public Integer getAttackingClubId() { return attackingClubId; }
    public void setAttackingClubId(Integer attackingClubId) { this.attackingClubId = attackingClubId; }

    public MatchDuelPlayerResponse getAttacker() { return attacker; }
    public void setAttacker(MatchDuelPlayerResponse attacker) { this.attacker = attacker; }

    public MatchDuelPlayerResponse getDefender() { return defender; }
    public void setDefender(MatchDuelPlayerResponse defender) { this.defender = defender; }

    public Boolean getGoal() { return goal; }
    public void setGoal(Boolean goal) { this.goal = goal; }
}
