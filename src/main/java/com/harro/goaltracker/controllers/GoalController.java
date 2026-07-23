package com.harro.goaltracker.controllers;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException.UnprocessableContent;
import org.springframework.web.util.UriComponentsBuilder;

import com.harro.goaltracker.mappers.GoalMapper;
import com.harro.goaltracker.repositories.GoalRepository;
import com.harro.goaltracker.repositories.TypeRepository;
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

    @GetMapping("/search")
    public List<GoalDto> searchGoals(@RequestParam(name = "query") String query){
        List<Goal> goals = goalRepository.searchGoalsByString(query);

        return goals.stream().map(goalMapper::toDto).toList();
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

    @PostMapping
    public ResponseEntity<GoalDto> createGoal(
        @RequestBody GoalDto request,
        UriComponentsBuilder uriBuilder
    ){
        request.setId(null);
        var goal = goalMapper.toEntity(request);

        if(request.getType() == null){
            return ResponseEntity.badRequest().build();
        }

        var type = typeRepository.findById(request.getType()).orElse(null);
        if(type == null){
            return new ResponseEntity<>(HttpStatusCode.valueOf(422));
        }
            goal.setType(type);
        goalRepository.save(goal);

        var goalDto = goalMapper.toDto(goal); 

        var uri = uriBuilder.path("/goals/{id}").buildAndExpand(goalDto.getId()).toUri();
        return ResponseEntity.created(uri).body(goalDto);
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
