package com.harro.goaltracker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harro.goaltracker.entities.Event;

public interface EventRepository extends JpaRepository<Event,Long>{
}
