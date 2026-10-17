package com.senai.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class ClubeResponse {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("acronym")
    private String acronym;

    @JsonProperty("cash")
    private BigDecimal cash;

    @JsonProperty("squad_value")
    private BigDecimal squadValue;

    @JsonProperty("net_worth")
    private BigDecimal netWorth;

    public ClubeResponse() {
    }

    public ClubeResponse(Integer id, String name, String acronym, BigDecimal cash, BigDecimal squadValue, BigDecimal netWorth) {
        this.id = id;
        this.name = name;
        this.acronym = acronym;
        this.cash = cash;
        this.squadValue = squadValue;
        this.netWorth = netWorth;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAcronym() { return acronym; }
    public void setAcronym(String acronym) { this.acronym = acronym; }

    public BigDecimal getCash() { return cash; }
    public void setCash(BigDecimal cash) { this.cash = cash; }

    public BigDecimal getSquadValue() { return squadValue; }
    public void setSquadValue(BigDecimal squadValue) { this.squadValue = squadValue; }

    public BigDecimal getNetWorth() { return netWorth; }
    public void setNetWorth(BigDecimal netWorth) { this.netWorth = netWorth; }
}
