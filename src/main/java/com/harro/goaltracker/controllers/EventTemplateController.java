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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.harro.goaltracker.dtos.EventTemplateDto;
import com.harro.goaltracker.entities.EventTemplate;
import com.harro.goaltracker.exceptions.InvalidReferenceException;
import com.harro.goaltracker.exceptions.NullAssignmentException;
import com.harro.goaltracker.mappers.EventTemplateMapper;
import com.harro.goaltracker.repositories.EventTemplateRepository;
import com.harro.goaltracker.repositories.GoalRepository;
import com.harro.goaltracker.repositories.ScheduleTemplateRepository;

import lombok.AllArgsConstructor;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/event-templates")
public class EventTemplateController {
    private final EventTemplateRepository eventTemplateRepository;
    private final GoalRepository goalRepository;
    private final ScheduleTemplateRepository scheduleTemplateRepository;
    private final EventTemplateMapper eventTemplateMapper;

    @GetMapping
    public List<EventTemplateDto> getAllEventTemplates(){
        List<EventTemplate> eventTemplates = eventTemplateRepository.findAll();

        return eventTemplates.stream().map(eventTemplateMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventTemplateDto> getEventTemplate(@PathVariable Long id){
        var eventTemplate = eventTemplateRepository.findById(id).orElse(null);

        if(eventTemplate == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(eventTemplateMapper.toDto(eventTemplate));
    }

    @PostMapping
    public ResponseEntity<EventTemplateDto> createEventTemplate(
        @RequestBody EventTemplateDto request,
        UriComponentsBuilder uriBuilder
    ){
        request.setId(null);
        var eventTemplate = eventTemplateMapper.toEntity(request);

        if(request.getScheduleTemplate() == null){
            throw new NullAssignmentException("scheduleTemplate");
        }

        var scheduleTemplate = scheduleTemplateRepository.findById(request.getScheduleTemplate()).orElse(null);
        if(scheduleTemplate == null){
            throw new InvalidReferenceException("scheduleTemplate");
        }
        eventTemplate.setScheduleTemplate(scheduleTemplate);

        if(request.getGoal() != null){
            var goal = goalRepository.findById(request.getGoal()).orElse(null);
            if(goal == null){
                throw new InvalidReferenceException("goal");
            }
            eventTemplate.setGoal(goal);
        }

        eventTemplateRepository.save(eventTemplate);

        var eventTemplateDto = eventTemplateMapper.toDto(eventTemplate);

        var uri = uriBuilder.path("/event-templates/{id}").buildAndExpand(eventTemplateDto.getId()).toUri();
        return ResponseEntity.created(uri).body(eventTemplateDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventTemplateDto> updateEventTemplate(
        @PathVariable(name="id") Long id,
        @RequestBody EventTemplateDto request
    ){
        var eventTemplate = eventTemplateRepository.findById(id).orElse(null);
        if(eventTemplate==null){
            return ResponseEntity.notFound().build();
        }

        if(request.getScheduleTemplate() == null){
            throw new NullAssignmentException("scheduleTemplate");
        }

        var scheduleTemplate = scheduleTemplateRepository.findById(request.getScheduleTemplate()).orElse(null);
        if(scheduleTemplate == null){
            throw new InvalidReferenceException("scheduleTemplate");
        }

        eventTemplate.setScheduleTemplate(scheduleTemplate);

        if(request.getGoal() != null){
            var goal = goalRepository.findById(request.getGoal()).orElse(null);
            if(goal == null){
                throw new InvalidReferenceException("goal");
            }
            eventTemplate.setGoal(goal);
        }else{
            eventTemplate.setGoal(null);
        }

        eventTemplateMapper.updateEventTemplate(request, eventTemplate);
        eventTemplateRepository.save(eventTemplate);
        return ResponseEntity.ok(eventTemplateMapper.toDto(eventTemplate));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EventTemplateDto> patchEventTemplate(
        @PathVariable(name="id") Long id,
        @RequestBody EventTemplateDto request
    ){
        var eventTemplate = eventTemplateRepository.findById(id).orElse(null);
        if(eventTemplate==null){
            return ResponseEntity.notFound().build();
        }

        if(request.getScheduleTemplate() != null){
            var scheduleTemplate = scheduleTemplateRepository.findById(request.getScheduleTemplate()).orElse(null);
            if(scheduleTemplate == null){
                throw new InvalidReferenceException("scheduleTemplate");
            }
            eventTemplate.setScheduleTemplate(scheduleTemplate);
        }

        if(request.getGoal() != null){
            var goal = goalRepository.findById(request.getGoal()).orElse(null);
            if(goal == null){
                throw new InvalidReferenceException("goal");
            }
            eventTemplate.setGoal(goal);
        }

        eventTemplateMapper.patchEventTemplate(request, eventTemplate);
        eventTemplateRepository.save(eventTemplate);
        return ResponseEntity.ok(eventTemplateMapper.toDto(eventTemplate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventTemplate(@PathVariable(name="id") Long id){
        var eventTemplate = eventTemplateRepository.findById(id).orElse(null);
        if(eventTemplate == null){
            return ResponseEntity.notFound().build();
        }

        eventTemplateRepository.delete(eventTemplate);
        return ResponseEntity.noContent().build();
    }
}
