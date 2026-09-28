package com.habitforge.habitforge.repository;

import com.habitforge.habitforge.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StreakRepository extends JpaRepository<Streak, Long> {

    Optional<Streak> findByHabitId(Long habitId);
}