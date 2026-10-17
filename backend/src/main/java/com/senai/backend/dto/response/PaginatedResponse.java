package com.senai.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaginatedResponse<T> {

    @JsonProperty("data")
    private List<T> data;

    @JsonProperty("meta")
    private PageMetaResponse meta;

    public PaginatedResponse() {
    }

    public PaginatedResponse(List<T> data, PageMetaResponse meta) {
        this.data = data;
        this.meta = meta;
    }

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }

    public PageMetaResponse getMeta() { return meta; }
    public void setMeta(PageMetaResponse meta) { this.meta = meta; }
}
