package com.senai.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TransacoesListResponse {

    @JsonProperty("data")
    private List<TransacaoResponse> data;

    @JsonProperty("summary")
    private TransacoesSummaryResponse summary;

    public TransacoesListResponse() {
    }

    public TransacoesListResponse(List<TransacaoResponse> data, TransacoesSummaryResponse summary) {
        this.data = data;
        this.summary = summary;
    }

    public List<TransacaoResponse> getData() { return data; }
    public void setData(List<TransacaoResponse> data) { this.data = data; }

    public TransacoesSummaryResponse getSummary() { return summary; }
    public void setSummary(TransacoesSummaryResponse summary) { this.summary = summary; }
}
