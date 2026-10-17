package com.senai.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.senai.backend.util.ValidationUtils;
import jakarta.validation.constraints.*;

import java.util.List;

public class JogadorUpdateRequest {

    @NotBlank(message = "O campo name é obrigatório.")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "O campo position é obrigatório.")
    @JsonProperty("position")
    private String position;

    @NotNull(message = "O campo agility é obrigatório.")
    @Min(value = 1, message = "A agilidade deve ser no mínimo 1.")
    @Max(value = 100, message = "A agilidade deve ser no máximo 100.")
    @JsonProperty("agility")
    private Integer agility;

    @NotNull(message = "O campo age é obrigatório.")
    @Min(value = 16, message = "A idade mínima é 16 anos.")
    @Max(value = 45, message = "A idade máxima é 45 anos.")
    @JsonProperty("age")
    private Integer age;

    public void validate() {
        this.name = ValidationUtils.validateAndSanitizePlayerName(this.name);

        List<String> validPositions = List.of("GOL", "ZAG", "LAT", "MEI", "ATA");
        if (!validPositions.contains(this.position)) {
            throw new IllegalArgumentException("Posição inválida. Valores aceitos: GOL, ZAG, LAT, MEI, ATA.");
        }

        if (this.age >= 35 && this.agility > 85) {
            throw new IllegalArgumentException("Jogadores com 35 anos ou mais podem ter agilidade de no máximo 85.");
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Integer getAgility() { return agility; }
    public void setAgility(Integer agility) { this.agility = agility; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
