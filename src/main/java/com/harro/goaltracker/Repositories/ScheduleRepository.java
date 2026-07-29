package com.harro.goaltracker.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harro.goaltracker.entities.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule,Long>{
    Optional<Schedule> getByDate(LocalDate date);
}
