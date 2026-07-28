package com.harro.goaltracker.controllers;

import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.harro.goaltracker.dtos.EventTemplateDto;
import com.harro.goaltracker.entities.EventTemplate;
import com.harro.goaltracker.mappers.EventTemplateMapper;
import com.harro.goaltracker.repositories.EventTemplateRepository;
import com.harro.goaltracker.repositories.GoalRepository;
import com.harro.goaltracker.repositories.ScheduleTemplateRepository;

import lombok.AllArgsConstructor;

@RestController
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
            return ResponseEntity.badRequest().build();
        }

        var scheduleTemplate = scheduleTemplateRepository.findById(request.getScheduleTemplate()).orElse(null);
        if(scheduleTemplate == null){
            return new ResponseEntity<>(HttpStatusCode.valueOf(422));
        }
        eventTemplate.setScheduleTemplate(scheduleTemplate);

        if(request.getGoal() != null){
            var goal = goalRepository.findById(request.getGoal()).orElse(null);
            if(goal == null){
                return new ResponseEntity<>(HttpStatusCode.valueOf(422));
            }
            eventTemplate.setGoal(goal);
        }

        eventTemplateRepository.save(eventTemplate);

        var eventTemplateDto = eventTemplateMapper.toDto(eventTemplate);

        var uri = uriBuilder.path("/event-templates/{id}").buildAndExpand(eventTemplateDto.getId()).toUri();
        return ResponseEntity.created(uri).body(eventTemplateDto);
    }
}
