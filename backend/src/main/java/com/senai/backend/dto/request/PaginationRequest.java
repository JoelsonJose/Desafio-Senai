package com.senai.backend.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.AssertTrue;

public class PaginationRequest {

    @Pattern(regexp = "[1-9][0-9]*", message = "O parâmetro page deve ser um inteiro positivo sem sinal ou zeros à esquerda.")
    private String page = "1";

    @Pattern(regexp = "([1-9]|[1-4][0-9]|50)", message = "O parâmetro limit deve ser um inteiro de 1 a 50, sem zeros à esquerda.")
    private String limit = "10";

    public String getPage() { return page; }

    public void setPage(String page) {
        this.page = page;
    }

    public String getLimit() { return limit; }

    public int getPageNumber() { return Integer.parseInt(page); }
    public int getLimitNumber() { return Integer.parseInt(limit); }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    @AssertTrue(message = "O parâmetro page não pode exceder o maior inteiro aceito.")
    public boolean isPageWithinIntegerRange() {
        try {
            return page != null && Integer.parseInt(page) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
