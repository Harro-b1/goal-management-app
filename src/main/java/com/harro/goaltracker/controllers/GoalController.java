package com.harro.goaltracker.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping List<GoalDto> getAllGoals(){
        List<Goal> goals = goalRepository.findAll();

        return goals.stream().map(goalMapper::toDto).toList();
    }
}
