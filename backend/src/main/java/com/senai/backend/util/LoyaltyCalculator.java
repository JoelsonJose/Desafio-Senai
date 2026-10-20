package com.senai.backend.util;

import java.time.Duration;
import java.time.LocalDateTime;

public final class LoyaltyCalculator {

    private LoyaltyCalculator() {
    }

    public static Calculation calculate(LocalDateTime reference, LocalDateTime loyaltyUntil) {
        long remainingHours = Duration.between(reference, loyaltyUntil).toHours();
        long remainingDays = remainingHours > 0
                ? (long) Math.ceil((double) remainingHours / 24.0)
                : 0;

        int penaltyPercentage = 0;
        if (remainingDays >= 1 && remainingDays <= 30) {
            penaltyPercentage = 5;
        } else if (remainingDays <= 180 && remainingDays >= 31) {
            penaltyPercentage = 10;
        } else if (remainingDays >= 181) {
            penaltyPercentage = 20;
        }

        return new Calculation(remainingDays, penaltyPercentage);
    }

    public record Calculation(long remainingDays, int penaltyPercentage) {
    }
}
