package com.harro.goaltracker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harro.goaltracker.entities.Category;

public interface CategoryRepository extends JpaRepository<Category,Long>{
}
