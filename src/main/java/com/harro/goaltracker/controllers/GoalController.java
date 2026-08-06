package com.harro.goaltracker.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.harro.goaltracker.dtos.GoalDto;
import com.harro.goaltracker.mappers.GoalMapper;
import com.harro.goaltracker.services.crud.GoalService;

import lombok.AllArgsConstructor;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/goals")
public class GoalController {
    private final GoalService goalService;
    private final GoalMapper goalMapper;

    @GetMapping
    public List<GoalDto> getAllGoals(){
        return goalService.getAllGoals().stream().map(goalMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalDto> getGoal(@PathVariable Long id){
        return goalService.getGoal(id)
            .map(goal -> ResponseEntity.ok(goalMapper.toDto(goal)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<GoalDto> createGoal(
        @RequestBody GoalDto request,
        UriComponentsBuilder uriBuilder
    ){
        var goal = goalService.createGoal(request);
        var goalDto = goalMapper.toDto(goal);

        var uri = uriBuilder.path("/goals/{id}").buildAndExpand(goalDto.getId()).toUri();
        return ResponseEntity.created(uri).body(goalDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDto> updateGoal(
        @PathVariable(name="id") Long id,
        @RequestBody GoalDto request
    ){
        return goalService.updateGoal(id, request)
            .map(goal -> ResponseEntity.ok(goalMapper.toDto(goal)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GoalDto> patchGoal(
        @PathVariable(name="id") Long id,
        @RequestBody GoalDto request
    ){
        return goalService.patchGoal(id, request)
            .map(goal -> ResponseEntity.ok(goalMapper.toDto(goal)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable(name="id") Long id){
        if(!goalService.deleteGoal(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<GoalDto> completeJob(
        @PathVariable(name = "id") Long id
    ){
        return goalService.completeGoal(id)
            .map(goal -> ResponseEntity.ok(goalMapper.toDto(goal)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/uncomplete")
    public ResponseEntity<GoalDto> uncompleteJob(
        @PathVariable(name = "id") Long id
    ){
        return goalService.uncompleteGoal(id)
            .map(goal -> ResponseEntity.ok(goalMapper.toDto(goal)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<GoalDto> searchGoals(@RequestParam(name = "query") String query){
        return goalService.searchGoals(query).stream().map(goalMapper::toDto).toList();
    }
}
