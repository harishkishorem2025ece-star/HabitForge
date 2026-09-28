package com.habitforge.habitforge.repository;

import com.habitforge.habitforge.entity.CompletionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CompletionLogRepository extends JpaRepository<CompletionLog, Long> {

    boolean existsByHabitIdAndCompletedDate(Long habitId, LocalDate completedDate);

    List<CompletionLog> findByHabitIdAndCompletedDateBetween(Long habitId, LocalDate from, LocalDate to);
}