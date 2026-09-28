package com.habitforge.habitforge.scheduler;

import com.habitforge.habitforge.entity.Frequency;
import com.habitforge.habitforge.entity.Habit;
import com.habitforge.habitforge.repository.CompletionLogRepository;
import com.habitforge.habitforge.repository.HabitRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
public class ReminderJob {

    private final HabitRepository habitRepo;
    private final CompletionLogRepository logRepo;

    public ReminderJob(HabitRepository habitRepo, CompletionLogRepository logRepo) {
        this.habitRepo = habitRepo;
        this.logRepo = logRepo;
    }

    // Runs every day at 8 PM (second minute hour day month weekday)
    @Scheduled(cron = "0 0 20 * * *")
    public void sendReminders() {
        LocalDate today = LocalDate.now();
        for (Habit habit : habitRepo.findAll()) {
            boolean requiredToday = habit.getFrequency() == Frequency.DAILY
                    || Arrays.asList(habit.getTargetDays().split(",")).contains(today.getDayOfWeek().name());
            boolean done = logRepo.existsByHabitIdAndCompletedDate(habit.getId(), today);
            if (requiredToday && !done) {
                System.out.println("REMINDER: Don't forget your habit '" + habit.getName() + "' today!");
            }
        }
    }
}