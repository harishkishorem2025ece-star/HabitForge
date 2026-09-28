package com.habitforge.habitforge.dto;

import java.time.LocalDate;

public record StreakResponse(Long habitId, int currentStreak, int bestStreak, LocalDate lastCompletedDate) {
}