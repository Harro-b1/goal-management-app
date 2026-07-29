package com.harro.goaltracker.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.harro.goaltracker.dtos.EventDto;
import com.harro.goaltracker.dtos.EventTemplateDto;
import com.harro.goaltracker.dtos.ScheduleTemplateDto;
import com.harro.goaltracker.entities.EventTemplate;
import com.harro.goaltracker.entities.ScheduleTemplate;
import com.harro.goaltracker.mappers.EventTemplateMapper;
import com.harro.goaltracker.mappers.ScheduleTemplateMapper;
import com.harro.goaltracker.repositories.EventTemplateRepository;
import com.harro.goaltracker.repositories.ScheduleTemplateRepository;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/schedule-templates")
public class ScheduleTemplateController {
    private final ScheduleTemplateRepository scheduleTemplateRepository;
    private final ScheduleTemplateMapper scheduleTemplateMapper;
    private final EventTemplateRepository eventTemplateRepository;
    private final EventTemplateMapper eventTemplateMapper;

    @GetMapping
    public List<ScheduleTemplateDto> getAllScheduleTemplates(){
        List<ScheduleTemplate> scheduleTemplates = scheduleTemplateRepository.findAll();

        return scheduleTemplates.stream().map(scheduleTemplateMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleTemplateDto> getScheduleTemplate(@PathVariable Long id){
        var scheduleTemplate = scheduleTemplateRepository.findById(id).orElse(null);

        if(scheduleTemplate == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(scheduleTemplateMapper.toDto(scheduleTemplate));
    }

    @GetMapping("/{id}/events")
    public List<EventTemplateDto> getScheduleTemplateContents(@PathVariable Long id){
        var scheduleTemplate = scheduleTemplateRepository.findById(id).orElse(null);
        if(scheduleTemplate == null){
            return new ArrayList<>();
        }
        List<EventTemplate> events = eventTemplateRepository.findByScheduleTemplate(scheduleTemplate);

        return events.stream().map(eventTemplateMapper::toDto).toList();
    }

    @PostMapping
    public ResponseEntity<ScheduleTemplateDto> createScheduleTemplate(
        @RequestBody ScheduleTemplateDto request,
        UriComponentsBuilder uriBuilder
    ){
        var scheduleTemplate = scheduleTemplateMapper.toEntity(request);
        scheduleTemplate.setId(null);
        scheduleTemplateRepository.save(scheduleTemplate);

        var scheduleTemplateDto = scheduleTemplateMapper.toDto(scheduleTemplate);

        var uri = uriBuilder.path("/schedule-templates/{id}").buildAndExpand(scheduleTemplateDto.getId()).toUri();
        return ResponseEntity.created(uri).body(scheduleTemplateDto);
    }
}
