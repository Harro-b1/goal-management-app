package com.harro.goaltracker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harro.goaltracker.entities.ScheduleTemplate;

public interface ScheduleTemplateRepository extends JpaRepository<ScheduleTemplate,Long>{
}
