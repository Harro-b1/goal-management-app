package com.harro.goaltracker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harro.goaltracker.entities.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule,Long>{
}
