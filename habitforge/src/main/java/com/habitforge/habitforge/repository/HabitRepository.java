package com.habitforge.habitforge.repository;

import com.habitforge.habitforge.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<Habit, Long> {
}