package com.harro.goaltracker.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.harro.goaltracker.mappers.EventMapper;
import com.harro.goaltracker.mappers.EventTemplateMapper;
import com.harro.goaltracker.mappers.GoalMapper;
import com.harro.goaltracker.repositories.GoalRepository;
import com.harro.goaltracker.repositories.CategoryRepository;
import com.harro.goaltracker.repositories.EventRepository;
import com.harro.goaltracker.repositories.EventTemplateRepository;

import dev.langchain4j.model.chat.ChatModel;

import com.harro.goaltracker.dtos.GoalDto;
import com.harro.goaltracker.entities.Goal;
import com.harro.goaltracker.exceptions.InvalidReferenceException;

import lombok.AllArgsConstructor;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/goals")
public class GoalController {
    private final GoalRepository goalRepository;
    private final CategoryRepository categoryRepository;
    private final ChatModel chatModel;
    private final GoalMapper goalMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventTemplateRepository eventTemplateRepository;
    private final EventTemplateMapper eventTemplateMapper;
    

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

    @PostMapping
    public ResponseEntity<GoalDto> createGoal(
        @RequestBody GoalDto request,
        UriComponentsBuilder uriBuilder
    ){
        var goal = goalMapper.toEntity(request);

        if(request.getCategory() != null){
            var category = categoryRepository.findById(request.getCategory()).orElse(null);
            if(category == null){
                throw new InvalidReferenceException("category");
            }
            goal.setCategory(category);
        }
        goalRepository.save(goal);

        var goalDto = goalMapper.toDto(goal); 

        var uri = uriBuilder.path("/goals/{id}").buildAndExpand(goalDto.getId()).toUri();
        return ResponseEntity.created(uri).body(goalDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDto> updateGoal(
        @PathVariable(name="id") Long id,
        @RequestBody GoalDto request
    ){
        var goal = goalRepository.findById(id).orElse(null);
        if(goal == null){
            return ResponseEntity.notFound().build();
        }
        
        if(request.getCategory() != null){
            var category = categoryRepository.findById(request.getCategory()).orElse(null);
            if(category == null){
                throw new InvalidReferenceException("category");
            }
            goal.setCategory(category);
        }else{
            goal.setCategory(null);
        }

        goalMapper.updateGoal(request, goal);
        goalRepository.save(goal);
        return ResponseEntity.ok(goalMapper.toDto(goal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable(name="id") Long id){
        var goal = goalRepository.findById(id).orElse(null);
        if(goal == null){
            return ResponseEntity.notFound().build();
        }

        var eventsWithGoal = eventRepository.findByGoal(goal);
        var strippedEvents = eventsWithGoal.stream().map(eventMapper::stripGoal).toList();
        eventRepository.saveAll(strippedEvents);

        var eventTemplatesWithGoal = eventTemplateRepository.findByGoal(goal);
        var strippedEventTemplates = eventTemplatesWithGoal.stream().map(eventTemplateMapper::stripGoal).toList();
        eventTemplateRepository.saveAll(strippedEventTemplates);

        goalRepository.delete(goal);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/chat")
    public ResponseEntity<String> testChat(@RequestParam(name = "query") String query){
        String response = chatModel.chat(query);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public List<GoalDto> searchGoals(@RequestParam(name = "query") String query){
        List<Goal> goals = goalRepository.searchGoalsByString(query);

        return goals.stream().map(goalMapper::toDto).toList();
    }
}
