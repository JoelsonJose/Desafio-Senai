package com.senai.backend.dto.request;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Converte e valida a sintaxe dos filtros recebidos como texto na query string. */
public class JogadorListQueryRequest {

    private static final Pattern POSITIVE_INTEGER = Pattern.compile("[1-9][0-9]*");
    private static final Pattern DECIMAL = Pattern.compile("(?:0|[1-9][0-9]*)(?:\\.[0-9]{1,2})?");

    private final String search;
    private final String position;
    private final String condition;
    private final String free;
    private final String clubId;
    private final String minAge;
    private final String maxAge;
    private final String minValue;
    private final String maxValue;
    private final String sort;

    public JogadorListQueryRequest(String search, String position, String condition, String free,
                                   String clubId, String minAge, String maxAge,
                                   String minValue, String maxValue, String sort) {
        this.search = search;
        this.position = position;
        this.condition = condition;
        this.free = free;
        this.clubId = clubId;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.sort = sort;
    }

    public void validateSyntax() {
        if (search != null && (search.trim().length() < 2 || search.trim().length() > 100)) {
            throw new IllegalArgumentException("O parâmetro search deve ter entre 2 e 100 caracteres.");
        }
        validateInteger(clubId, "club_id");
        validateRangeInteger(minAge, "min_age", 16, 45);
        validateRangeInteger(maxAge, "max_age", 16, 45);
        validateDecimal(minValue, "min_value");
        validateDecimal(maxValue, "max_value");
        if (free != null && !"true".equals(free) && !"false".equals(free)) {
            throw new IllegalArgumentException("O parâmetro free deve ser true ou false.");
        }
    }

    private void validateInteger(String value, String parameter) {
        if (value != null && (!POSITIVE_INTEGER.matcher(value).matches() || !fitsInteger(value))) {
            throw new IllegalArgumentException("O parâmetro " + parameter + " deve ser um inteiro positivo sem sinal ou zeros à esquerda.");
        }
    }

    private void validateRangeInteger(String value, String parameter, int min, int max) {
        if (value == null) return;
        validateInteger(value, parameter);
        int parsed = Integer.parseInt(value);
        if (parsed < min || parsed > max) {
            throw new IllegalArgumentException("O parâmetro " + parameter + " deve estar entre " + min + " e " + max + ".");
        }
    }

    private void validateDecimal(String value, String parameter) {
        if (value == null) return;
        if (!DECIMAL.matcher(value).matches()) {
            throw new IllegalArgumentException("O parâmetro " + parameter + " deve ser decimal sem sinal, expoente ou zeros à esquerda, com até 2 casas.");
        }
        if (new BigDecimal(value).compareTo(new BigDecimal("550")) > 0) {
            throw new IllegalArgumentException("O parâmetro " + parameter + " deve estar entre 0 e 550.");
        }
    }

    private boolean fitsInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public String getSearch() { return search; }
    public String getPosition() { return position; }
    public String getCondition() { return condition; }
    public Boolean getFree() { return free == null ? null : Boolean.valueOf(free); }
    public Integer getClubId() { return clubId == null ? null : Integer.valueOf(clubId); }
    public Integer getMinAge() { return minAge == null ? null : Integer.valueOf(minAge); }
    public Integer getMaxAge() { return maxAge == null ? null : Integer.valueOf(maxAge); }
    public BigDecimal getMinValue() { return minValue == null ? null : new BigDecimal(minValue); }
    public BigDecimal getMaxValue() { return maxValue == null ? null : new BigDecimal(maxValue); }
    public String getSort() { return sort; }
}
