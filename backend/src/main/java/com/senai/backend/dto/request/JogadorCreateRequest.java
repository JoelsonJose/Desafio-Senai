package com.senai.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.senai.backend.util.ValidationUtils;
import jakarta.validation.constraints.*;

import java.util.List;

public class JogadorCreateRequest {

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

    @NotNull(message = "O campo pace é obrigatório.")
    @Min(1) @Max(99)
    @JsonProperty("pace")
    private Integer pace;

    @NotNull(message = "O campo shooting é obrigatório.")
    @Min(1) @Max(99)
    @JsonProperty("shooting")
    private Integer shooting;

    @NotNull(message = "O campo passing é obrigatório.")
    @Min(1) @Max(99)
    @JsonProperty("passing")
    private Integer passing;

    @NotNull(message = "O campo dribbling é obrigatório.")
    @Min(1) @Max(99)
    @JsonProperty("dribbling")
    private Integer dribbling;

    @NotNull(message = "O campo defending é obrigatório.")
    @Min(1) @Max(99)
    @JsonProperty("defending")
    private Integer defending;

    @NotNull(message = "O campo physical é obrigatório.")
    @Min(1) @Max(99)
    @JsonProperty("physical")
    private Integer physical;

    @NotNull(message = "O campo potential é obrigatório.")
    @Min(1) @Max(99)
    @JsonProperty("potential")
    private Integer potential;

    public void validate() {
        this.name = ValidationUtils.validateAndSanitizePlayerName(this.name);

        List<String> validPositions = List.of("GOL", "ZAG", "LAT", "MEI", "ATA");
        if (!validPositions.contains(this.position)) {
            throw new IllegalArgumentException("Posição inválida. Valores aceitos: GOL, ZAG, LAT, MEI, ATA.");
        }

        if (this.age >= 35 && this.agility > 85) {
            throw new IllegalArgumentException("Jogadores com 35 anos ou mais podem ter agilidade de no máximo 85.");
        }

        int somaAtributos = this.pace + this.shooting + this.passing + this.dribbling + this.defending + this.physical;
        if (somaAtributos > 480) {
            throw new IllegalArgumentException("A soma dos seis atributos técnicos não pode ultrapassar 480.");
        }

        int maiorAtributo = Math.max(this.pace,
                Math.max(this.shooting,
                        Math.max(this.passing,
                                Math.max(this.dribbling,
                                        Math.max(this.defending, this.physical)))));

        if (this.potential < maiorAtributo) {
            throw new IllegalArgumentException("O potencial (" + this.potential + ") não pode ser menor que o maior atributo do jogador (" + maiorAtributo + ").");
        }

        if (this.age >= 33 && this.potential != maiorAtributo) {
            throw new IllegalArgumentException("No cadastro de jogador com 33 anos ou mais, o potencial deve ser igual ao maior dos seis atributos (" + maiorAtributo + ").");
        }
    }

    // Getters e Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Integer getAgility() { return agility; }
    public void setAgility(Integer agility) { this.agility = agility; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Integer getPace() { return pace; }
    public void setPace(Integer pace) { this.pace = pace; }

    public Integer getShooting() { return shooting; }
    public void setShooting(Integer shooting) { this.shooting = shooting; }

    public Integer getPassing() { return passing; }
    public void setPassing(Integer passing) { this.passing = passing; }

    public Integer getDribbling() { return dribbling; }
    public void setDribbling(Integer dribbling) { this.dribbling = dribbling; }

    public Integer getDefending() { return defending; }
    public void setDefending(Integer defending) { this.defending = defending; }

    public Integer getPhysical() { return physical; }
    public void setPhysical(Integer physical) { this.physical = physical; }

    public Integer getPotential() { return potential; }
    public void setPotential(Integer potential) { this.potential = potential; }
}
