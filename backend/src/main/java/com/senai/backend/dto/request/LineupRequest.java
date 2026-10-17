package com.senai.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.List;

public class LineupRequest {

    @NotBlank(message = "O campo formation é obrigatório.")
    @JsonProperty("formation")
    private String formation;

    @NotNull(message = "O campo player_ids é obrigatório.")
    @JsonProperty("player_ids")
    private List<Integer> playerIds;

    public void validate() {
        List<String> validFormations = List.of("3-1", "2-2", "2-1-1", "1-2-1");
        if (this.formation == null || !validFormations.contains(this.formation)) {
            throw new IllegalArgumentException("Formação tática inválida. Valores aceitos: 3-1, 2-2, 2-1-1, 1-2-1.");
        }

        if (this.playerIds == null || this.playerIds.size() != 5) {
            throw new IllegalArgumentException("A lista player_ids deve conter exatamente 5 jogadores.");
        }

        for (Integer id : this.playerIds) {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Cada identificador em player_ids deve ser um inteiro positivo.");
            }
        }

        if (new HashSet<>(this.playerIds).size() != 5) {
            throw new IllegalArgumentException("Os 5 identificadores de jogadores devem ser distintos.");
        }
    }

    public String getFormation() { return formation; }
    public void setFormation(String formation) { this.formation = formation; }

    public List<Integer> getPlayerIds() { return playerIds; }
    public void setPlayerIds(List<Integer> playerIds) { this.playerIds = playerIds; }
}
