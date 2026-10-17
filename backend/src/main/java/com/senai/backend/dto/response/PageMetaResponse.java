package com.senai.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PageMetaResponse {

    @JsonProperty("page")
    private int page;

    @JsonProperty("limit")
    private int limit;

    @JsonProperty("total_items")
    private long totalItems;

    @JsonProperty("total_pages")
    private int totalPages;

    public PageMetaResponse() {
    }

    public PageMetaResponse(int page, int limit, long totalItems, int totalPages) {
        this.page = page;
        this.limit = limit;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
    }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
