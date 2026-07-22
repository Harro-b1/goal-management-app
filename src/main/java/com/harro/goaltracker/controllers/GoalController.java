package com.harro.goaltracker.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harro.goaltracker.Repositories.GoalRepository;
import com.harro.goaltracker.Repositories.TypeRepository;
import com.harro.goaltracker.mappers.GoalMapper;
import com.harro.goaltracker.dtos.GoalDto;
import com.harro.goaltracker.entities.Goal;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/goals")
public class GoalController {
    private final GoalRepository goalRepository;
    private final TypeRepository typeRepository;
    
    private final GoalMapper goalMapper;

    @GetMapping 
    public List<GoalDto> getAllGoals(){
        List<Goal> goals = goalRepository.findAll();

        return goals.stream().map(goalMapper::toDto).toList();
    }

    @GetMapping("/{id}") 
    public ResponseEntity<GoalDto> getGoal(@PathVariable Long id){
        var goal = goalRepository.findById(id).orElse(null);
        if (goal == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(goalMapper.toDto(goal));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<GoalDto> completeJob(
        @PathVariable(name = "id") Long id
    ){
        var goal = goalRepository.findById(id).orElse(null);
        
        if(goal == null){
            return ResponseEntity.notFound().build();
        }
        goal.setCompleted(true);
        goalRepository.save(goal);

        return ResponseEntity.ok(goalMapper.toDto(goal));
    }

    @PutMapping("/{id}/uncomplete")
    public ResponseEntity<GoalDto> uncompleteJob(
        @PathVariable(name = "id") Long id
    ){
        var goal = goalRepository.findById(id).orElse(null);
        
        if(goal == null){
            return ResponseEntity.notFound().build();
        }
        goal.setCompleted(false);
        goalRepository.save(goal);

        return ResponseEntity.ok(goalMapper.toDto(goal));
    }
}
