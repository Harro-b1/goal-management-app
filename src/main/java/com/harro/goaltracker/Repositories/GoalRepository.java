package com.harro.goaltracker.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.harro.goaltracker.entities.Category;
import com.harro.goaltracker.entities.Goal;


public interface GoalRepository extends JpaRepository<Goal,Long>{
    @Query(value = "SELECT * FROM goals WHERE name REGEXP :value OR description REGEXP :value",
     nativeQuery = true)
    List<Goal> searchGoalsByString(@Param("value") String value);

    List<Goal> findAllByCategory(Category category);

    @Query("SELECT g FROM Goal g ORDER BY g.priority DESC, g.finishByDate ASC NULLS LAST")
    List<Goal> findAllOrderedByPriorityAndFinishByDate();
}
