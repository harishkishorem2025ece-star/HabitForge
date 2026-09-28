package com.habitforge.habitforge.controller;

import com.habitforge.habitforge.dto.CalendarResponse;
import com.habitforge.habitforge.dto.CompletionRequest;
import com.habitforge.habitforge.dto.HabitRequest;
import com.habitforge.habitforge.dto.StreakResponse;
import com.habitforge.habitforge.entity.Habit;
import com.habitforge.habitforge.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService service;

    public HabitController(HabitService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Habit> createHabit(@Valid @RequestBody HabitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createHabit(request));
    }

    @GetMapping
    public List<Habit> getAllHabits() {
        return service.getAllHabits();
    }

    @GetMapping("/{id}")
    public Habit getHabit(@PathVariable Long id) {
        return service.getHabit(id);
    }

    @PostMapping("/{id}/completions")
    public StreakResponse logCompletion(@PathVariable Long id,
                                        @RequestBody(required = false) CompletionRequest request) {
        return service.logCompletion(id, request == null ? null : request.date());
    }

    @GetMapping("/{id}/streak")
    public StreakResponse getStreak(@PathVariable Long id) {
        return service.getStreak(id);
    }

    @GetMapping("/{id}/calendar")
    public CalendarResponse getCalendar(@PathVariable Long id,
                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.getCalendar(id, month);
    }
}