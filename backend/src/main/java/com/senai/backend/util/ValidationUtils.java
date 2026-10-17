package com.senai.backend.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}]");
    private static final Pattern DIGITS = Pattern.compile(".*\\d.*");
    private static final Pattern CLUB_NAME_ALLOWED = Pattern.compile("^[\\p{L} .\\-]+$");

    private ValidationUtils() {
    }

    /**
     * V4: Remove espaços no início e no fim, e reduz espaços repetidos a um só.
     * Rejeita caracteres de controle com IllegalArgumentException (400).
     */
    public static String sanitizeText(String text) {
        if (text == null) {
            return null;
        }

        if (CONTROL_CHARS.matcher(text).find()) {
            throw new IllegalArgumentException("O texto contém caracteres de controle inválidos.");
        }

        String sanitized = text.replaceAll("\\s+", " ").trim();
        return sanitized.isEmpty() ? null : sanitized;
    }

    public static String stripAccents(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    /**
     * Valida o nome do jogador conforme regra V4 e Limites de cada campo:
     * - 3 a 100 caracteres após higienização.
     * - Pelo menos duas palavras (sequência com pelo menos uma letra Unicode).
     * - Nenhum dígito.
     */
    public static String validateAndSanitizePlayerName(String name) {
        String sanitized = sanitizeText(name);
        if (sanitized == null || sanitized.isEmpty()) {
            throw new IllegalArgumentException("Nome do jogador é obrigatório.");
        }

        if (sanitized.length() < 3 || sanitized.length() > 100) {
            throw new IllegalArgumentException("O nome do jogador deve ter entre 3 e 100 caracteres.");
        }

        if (DIGITS.matcher(sanitized).find()) {
            throw new IllegalArgumentException("O nome do jogador não pode conter dígitos.");
        }

        String[] parts = sanitized.split(" ");
        int countWords = 0;
        for (String p : parts) {
            // Palavra: sequência com pelo menos uma letra
            if (p.matches(".*\\p{L}.*")) {
                countWords++;
            }
        }

        if (countWords < 2) {
            throw new IllegalArgumentException("O nome do jogador deve conter pelo menos duas palavras.");
        }

        return sanitized;
    }

    /**
     * Valida o nome do clube conforme regra V4 e Limites de cada campo:
     * - 3 a 60 caracteres.
     * - 2 a 5 palavras.
     * - Somente letras (com ou sem acento), espaços, hífen e ponto.
     */
    public static String validateAndSanitizeClubName(String name) {
        String sanitized = sanitizeText(name);
        if (sanitized == null || sanitized.isEmpty()) {
            throw new IllegalArgumentException("Nome do clube é obrigatório.");
        }

        if (sanitized.length() < 3 || sanitized.length() > 60) {
            throw new IllegalArgumentException("O nome do clube deve ter entre 3 e 60 caracteres.");
        }

        if (!CLUB_NAME_ALLOWED.matcher(sanitized).matches()) {
            throw new IllegalArgumentException("O nome do clube deve conter apenas letras, espaços, hífen e ponto.");
        }

        String[] parts = sanitized.split(" ");
        int countWords = 0;
        for (String p : parts) {
            if (p.matches(".*\\p{L}.*")) {
                countWords++;
            }
        }

        if (countWords < 2 || countWords > 5) {
            throw new IllegalArgumentException("O nome do clube deve conter entre 2 e 5 palavras.");
        }

        return sanitized;
    }

    /**
     * Valida a sigla do clube conforme Limites de cada campo:
     * - Exatamente 3 letras de A a Z, maiúsculas e sem acento.
     * - Não podem ser todas iguais.
     * - A primeira letra deve ser a primeira letra do nome do clube.
     * - As 3 letras devem aparecer no nome do clube nessa ordem.
     */
    public static void validateClubAcronym(String acronym, String clubName) {
        if (acronym == null || acronym.length() != 3 || !acronym.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("A sigla do clube deve conter exatamente 3 letras maiúsculas de A a Z sem acento.");
        }

        if (acronym.charAt(0) == acronym.charAt(1) && acronym.charAt(1) == acronym.charAt(2)) {
            throw new IllegalArgumentException("As três letras da sigla não podem ser todas iguais.");
        }

        String strippedName = stripAccents(clubName).toUpperCase();
        
        // Primeira letra da sigla deve ser a primeira letra do nome
        char firstLetterName = ' ';
        for (int i = 0; i < strippedName.length(); i++) {
            char c = strippedName.charAt(i);
            if (Character.isLetter(c)) {
                firstLetterName = c;
                break;
            }
        }

        if (acronym.charAt(0) != firstLetterName) {
            throw new IllegalArgumentException("A primeira letra da sigla deve ser a primeira letra do nome do clube.");
        }

        // As 3 letras devem aparecer no nome nessa ordem
        int idx0 = strippedName.indexOf(acronym.charAt(0));
        int idx1 = idx0 != -1 ? strippedName.indexOf(acronym.charAt(1), idx0 + 1) : -1;
        int idx2 = idx1 != -1 ? strippedName.indexOf(acronym.charAt(2), idx1 + 1) : -1;

        if (idx0 == -1 || idx1 == -1 || idx2 == -1) {
            throw new IllegalArgumentException("As letras da sigla devem aparecer no nome do clube na mesma ordem.");
        }
    }
}
