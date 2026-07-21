package com.harro.goaltracker.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harro.goaltracker.Repositories.GoalRepository;
import com.harro.goaltracker.mappers.GoalMapper;
import com.harro.goaltracker.dtos.GoalDto;
import com.harro.goaltracker.entities.Goal;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/goals")
public class GoalController {
    private final GoalRepository goalRepository;
    
    private final GoalMapper goalMapper;

    @GetMapping 
    public List<GoalDto> getAllGoals(){
        List<Goal> goals = goalRepository.findAll();

        return goals.stream().map(goalMapper::toDto).toList();
    }

    @GetMapping("/{id}") ResponseEntity<GoalDto> getGoal(@PathVariable Long id){
        var goal = goalRepository.findById(id).orElse(null);
        if (goal == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(goalMapper.toDto(goal));
    }
}
