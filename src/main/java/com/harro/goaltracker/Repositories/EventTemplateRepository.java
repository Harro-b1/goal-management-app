package com.harro.goaltracker.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harro.goaltracker.entities.EventTemplate;
import com.harro.goaltracker.entities.ScheduleTemplate;

public interface EventTemplateRepository extends JpaRepository<EventTemplate,Long>{
    List<EventTemplate> findByScheduleTemplate(ScheduleTemplate scheduleTemplate);
}
