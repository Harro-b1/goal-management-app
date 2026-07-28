package com.harro.goaltracker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harro.goaltracker.entities.EventTemplate;

public interface EventTemplateRepository extends JpaRepository<EventTemplate,Long>{
}
