package com.harro.goaltracker.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harro.goaltracker.entities.Event;
import com.harro.goaltracker.entities.Schedule;

public interface EventRepository extends JpaRepository<Event,Long>{
    List<Event> findBySchedule(Schedule schedule);
}
