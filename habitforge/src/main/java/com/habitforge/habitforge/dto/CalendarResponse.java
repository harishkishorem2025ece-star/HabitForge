package com.habitforge.habitforge.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarResponse(String month, int completedCount, List<LocalDate> completedDates) {
}