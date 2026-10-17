package com.senai.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class CondicaoRequest {

    @NotBlank(message = "O campo condition é obrigatório.")
    @JsonProperty("condition")
    private String condition;

    public void validate() {
        List<String> validConditions = List.of("APTO", "MAL_CONDICIONADO", "DOENTE");
        if (this.condition == null || !validConditions.contains(this.condition)) {
            throw new IllegalArgumentException("Condição física inválida. Valores aceitos: APTO, MAL_CONDICIONADO, DOENTE.");
        }
    }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}
