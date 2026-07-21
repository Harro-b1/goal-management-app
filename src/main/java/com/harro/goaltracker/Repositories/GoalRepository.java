package com.harro.goaltracker.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harro.goaltracker.entities.Goal;


public interface GoalRepository extends JpaRepository<Goal,Long>{
}
