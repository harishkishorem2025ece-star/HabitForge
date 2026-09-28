package com.habitforge.habitforge.service;

import com.habitforge.habitforge.dto.CalendarResponse;
import com.habitforge.habitforge.dto.HabitRequest;
import com.habitforge.habitforge.dto.StreakResponse;
import com.habitforge.habitforge.entity.CompletionLog;
import com.habitforge.habitforge.entity.Frequency;
import com.habitforge.habitforge.entity.Habit;
import com.habitforge.habitforge.entity.Streak;
import com.habitforge.habitforge.exception.BusinessRuleException;
import com.habitforge.habitforge.exception.ResourceNotFoundException;
import com.habitforge.habitforge.repository.CompletionLogRepository;
import com.habitforge.habitforge.repository.HabitRepository;
import com.habitforge.habitforge.repository.StreakRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HabitService {

    private final HabitRepository habitRepo;
    private final CompletionLogRepository logRepo;
    private final StreakRepository streakRepo;

    public HabitService(HabitRepository habitRepo,
                        CompletionLogRepository logRepo,
                        StreakRepository streakRepo) {
        this.habitRepo = habitRepo;
        this.logRepo = logRepo;
        this.streakRepo = streakRepo;
    }

    // ---------- Feature 1: create a habit ----------
    @Transactional
    public Habit createHabit(HabitRequest r) {
        Habit habit = new Habit();
        habit.setName(r.name().trim());
        habit.setFrequency(r.frequency());
        habit.setStartDate(r.startDate() == null ? LocalDate.now() : r.startDate());

        if (r.frequency() == Frequency.SPECIFIC_DAYS) {
            if (r.targetDays() == null || r.targetDays().isEmpty()) {
                throw new BusinessRuleException("targetDays is required for SPECIFIC_DAYS habits");
            }
            habit.setTargetDays(r.targetDays().stream()
                    .distinct()
                    .map(DayOfWeek::name)
                    .collect(Collectors.joining(",")));
        }

        Habit saved = habitRepo.save(habit);

        Streak streak = new Streak();
        streak.setHabit(saved);
        streakRepo.save(streak);
        return saved;
    }

    public List<Habit> getAllHabits() {
        return habitRepo.findAll();
    }

    public Habit getHabit(Long id) {
        return habitRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found with id " + id));
    }

    // ---------- Features 2, 3, 4: log a day, compute streaks ----------
    @Transactional
    public StreakResponse logCompletion(Long habitId, LocalDate date) {
        Habit habit = getHabit(habitId);
        LocalDate today = LocalDate.now();
        LocalDate day = (date == null) ? today : date;

        // Validation: reject bad requests BEFORE touching the data
        if (day.isAfter(today)) {
            throw new BusinessRuleException("Cannot log a future date: " + day);
        }
        if (day.isBefore(habit.getStartDate())) {
            throw new BusinessRuleException("Cannot log a date before the habit start date (" + habit.getStartDate() + ")");
        }
        if (!isRequiredDay(habit, day)) {
            throw new BusinessRuleException(day + " (" + day.getDayOfWeek() + ") is not a scheduled day for this habit");
        }
        if (logRepo.existsByHabitIdAndCompletedDate(habitId, day)) {
            throw new BusinessRuleException("Habit already logged for " + day);
        }

        Streak streak = getStreakEntity(habitId);
        LocalDate last = streak.getLastCompletedDate();
        if (last != null && day.isBefore(last)) {
            throw new BusinessRuleException("Cannot log a date before the last completed date (" + last + ")");
        }

        // Streak rule: continue only if the previous required day was completed, otherwise reset to 1
        if (last != null && last.equals(previousRequiredDay(habit, day))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            streak.setCurrentStreak(1);
        }

        // Best-streak rule: update only when current exceeds it
        if (streak.getCurrentStreak() > streak.getBestStreak()) {
            streak.setBestStreak(streak.getCurrentStreak());
        }
        streak.setLastCompletedDate(day);

        CompletionLog log = new CompletionLog();
        log.setHabit(habit);
        log.setCompletedDate(day);
        logRepo.save(log);
        streakRepo.save(streak);

        return toResponse(habit, streak);
    }

    // ---------- Feature 3/4: view current and best streak ----------
    public StreakResponse getStreak(Long habitId) {
        Habit habit = getHabit(habitId);
        return toResponse(habit, getStreakEntity(habitId));
    }

    // ---------- Feature 5: monthly completion calendar ----------
    public CalendarResponse getCalendar(Long habitId, YearMonth month) {
        getHabit(habitId);
        List<LocalDate> dates = logRepo
                .findByHabitIdAndCompletedDateBetween(habitId, month.atDay(1), month.atEndOfMonth())
                .stream()
                .map(CompletionLog::getCompletedDate)
                .sorted()
                .toList();
        return new CalendarResponse(month.toString(), dates.size(), dates);
    }

    // ---------- Helpers ----------
    private Streak getStreakEntity(Long habitId) {
        return streakRepo.findByHabitId(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Streak record not found for habit " + habitId));
    }

    private boolean isRequiredDay(Habit habit, LocalDate date) {
        if (habit.getFrequency() == Frequency.DAILY) {
            return true;
        }
        return Arrays.asList(habit.getTargetDays().split(","))
                .contains(date.getDayOfWeek().name());
    }

    // The closest required day strictly before the given date
    private LocalDate previousRequiredDay(Habit habit, LocalDate date) {
        LocalDate d = date.minusDays(1);
        while (!isRequiredDay(habit, d)) {
            d = d.minusDays(1);
        }
        return d;
    }

    // Current streak as it should be shown today: 0 if a required day was missed
    private int effectiveCurrentStreak(Habit habit, Streak streak) {
        LocalDate last = streak.getLastCompletedDate();
        if (last == null) {
            return 0;
        }
        if (last.isBefore(previousRequiredDay(habit, LocalDate.now()))) {
            return 0;
        }
        return streak.getCurrentStreak();
    }

    private StreakResponse toResponse(Habit habit, Streak streak) {
        return new StreakResponse(
                habit.getId(),
                effectiveCurrentStreak(habit, streak),
                streak.getBestStreak(),
                streak.getLastCompletedDate());
    }
}