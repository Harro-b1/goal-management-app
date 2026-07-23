package com.harro.goaltracker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harro.goaltracker.entities.Type;

public interface TypeRepository extends JpaRepository<Type,Long>{
}
