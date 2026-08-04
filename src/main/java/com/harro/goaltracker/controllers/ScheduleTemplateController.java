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
import com.harro.goaltracker.dtos.ScheduleTemplateDto;
import com.harro.goaltracker.mappers.EventTemplateMapper;
import com.harro.goaltracker.mappers.ScheduleTemplateMapper;
import com.harro.goaltracker.services.ScheduleTemplateService;

import lombok.AllArgsConstructor;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/schedule-templates")
public class ScheduleTemplateController {
    private final ScheduleTemplateService scheduleTemplateService;
    private final ScheduleTemplateMapper scheduleTemplateMapper;
    private final EventTemplateMapper eventTemplateMapper;

    @GetMapping
    public List<ScheduleTemplateDto> getAllScheduleTemplates(){
        return scheduleTemplateService.getAllScheduleTemplates().stream().map(scheduleTemplateMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleTemplateDto> getScheduleTemplate(@PathVariable Long id){
        return scheduleTemplateService.getScheduleTemplate(id)
            .map(scheduleTemplate -> ResponseEntity.ok(scheduleTemplateMapper.toDto(scheduleTemplate)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/events")
    public List<EventTemplateDto> getScheduleTemplateContents(@PathVariable Long id){
        return scheduleTemplateService.getScheduleTemplateContents(id).stream().map(eventTemplateMapper::toDto).toList();
    }

    @PostMapping
    public ResponseEntity<ScheduleTemplateDto> createScheduleTemplate(
        @RequestBody ScheduleTemplateDto request,
        UriComponentsBuilder uriBuilder
    ){
        var scheduleTemplate = scheduleTemplateService.createScheduleTemplate(request);
        var scheduleTemplateDto = scheduleTemplateMapper.toDto(scheduleTemplate);

        var uri = uriBuilder.path("/schedule-templates/{id}").buildAndExpand(scheduleTemplateDto.getId()).toUri();
        return ResponseEntity.created(uri).body(scheduleTemplateDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleTemplateDto> updateScheduleTemplate(
        @PathVariable (name="id") Long id,
        @RequestBody ScheduleTemplateDto request
    ){
        return scheduleTemplateService.updateScheduleTemplate(id, request)
            .map(scheduleTemplate -> ResponseEntity.ok(scheduleTemplateMapper.toDto(scheduleTemplate)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ScheduleTemplateDto> patchScheduleTemplate(
        @PathVariable (name="id") Long id,
        @RequestBody ScheduleTemplateDto request
    ){
        return scheduleTemplateService.patchScheduleTemplate(id, request)
            .map(scheduleTemplate -> ResponseEntity.ok(scheduleTemplateMapper.toDto(scheduleTemplate)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScheduleTemplate(@PathVariable(name="id") Long id){
        if(!scheduleTemplateService.deleteScheduleTemplate(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
