package com.habitforge.habitforge.dto;

import com.habitforge.habitforge.entity.Frequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record HabitRequest(
        @NotBlank(message = "Habit name is required") String name,
        @NotNull(message = "Frequency is required (DAILY or SPECIFIC_DAYS)") Frequency frequency,
        List<DayOfWeek> targetDays,
        @PastOrPresent(message = "Start date cannot be in the future") LocalDate startDate) {
}