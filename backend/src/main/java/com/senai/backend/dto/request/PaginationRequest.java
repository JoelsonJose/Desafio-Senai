package com.senai.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PaginationRequest {

    @Min(value = 1, message = "O parâmetro page deve ser maior ou igual a 1.")
    private int page = 1;

    @Min(value = 1, message = "O parâmetro limit deve estar entre 1 e 100.")
    @Max(value = 100, message = "O parâmetro limit deve estar entre 1 e 100.")
    private int limit = 10;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
