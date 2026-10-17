package com.senai.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.senai.backend.util.ValidationUtils;
import jakarta.validation.constraints.NotBlank;

public class ClubeCreateRequest {

    @NotBlank(message = "O campo name é obrigatório.")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "O campo acronym é obrigatório.")
    @JsonProperty("acronym")
    private String acronym;

    public void validate() {
        this.name = ValidationUtils.validateAndSanitizeClubName(this.name);
        if (this.acronym != null) {
            this.acronym = this.acronym.trim();
        }
        ValidationUtils.validateClubAcronym(this.acronym, this.name);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAcronym() { return acronym; }
    public void setAcronym(String acronym) { this.acronym = acronym; }
}
