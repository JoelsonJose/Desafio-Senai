package com.senai.backend.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class DateTimeUtils {

    public static final ZoneOffset BRASILIA_OFFSET = ZoneOffset.ofHours(-3);
    public static final ZoneId BRASILIA_ZONE = ZoneId.of("-03:00");

    // Formato de resposta: AAAA-MM-DDTHH:MM:SS-03:00
    private static final DateTimeFormatter RESPONSE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    // Formato estrito de requisição: AAAA-MM-DDTHH:MM-03:00 (com ResolverStyle.STRICT para rejeitar datas como 30 de fevereiro)
    private static final DateTimeFormatter REQUEST_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mmXXX")
            .withResolverStyle(ResolverStyle.STRICT);

    private DateTimeUtils() {
    }

    public static OffsetDateTime now() {
        return OffsetDateTime.now(BRASILIA_ZONE);
    }

    public static OffsetDateTime parseRequestDateTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Data e hora não informada.");
        }
        
        // RN01: deve terminar com -03:00 obrigatoriamente
        if (!text.endsWith("-03:00")) {
            throw new IllegalArgumentException("A data e hora deve estar no fuso horário de Brasília (-03:00).");
        }

        try {
            return OffsetDateTime.parse(text, REQUEST_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data e hora inválido ou data inexistente. Formato esperado: AAAA-MM-DDTHH:MM-03:00");
        }
    }

    public static String formatResponse(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.withOffsetSameInstant(BRASILIA_OFFSET).format(RESPONSE_FORMATTER);
    }

    public static String formatResponse(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atOffset(BRASILIA_OFFSET).format(RESPONSE_FORMATTER);
    }
}
