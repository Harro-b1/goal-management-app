package com.harro.goaltracker.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.harro.goaltracker.dtos.EventDto;
import com.harro.goaltracker.dtos.ScheduleDto;
import com.harro.goaltracker.entities.Event;
import com.harro.goaltracker.entities.EventTemplate;
import com.harro.goaltracker.entities.Schedule;
import com.harro.goaltracker.mappers.EventMapper;
import com.harro.goaltracker.mappers.ScheduleMapper;
import com.harro.goaltracker.repositories.EventRepository;
import com.harro.goaltracker.repositories.EventTemplateRepository;
import com.harro.goaltracker.repositories.ScheduleRepository;
import com.harro.goaltracker.repositories.ScheduleTemplateRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/schedules")
public class ScheduleController {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final ScheduleTemplateRepository scheduleTemplateRepository;
    private final EventTemplateRepository eventTemplateRepository;

    @GetMapping
    public List<ScheduleDto> getAllSchedules(){
        List<Schedule> schedules = scheduleRepository.findAll();

        return schedules.stream().map(scheduleMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDto> getSchedule(@PathVariable Long id){
        var schedule = scheduleRepository.findById(id).orElse(null);

        if(schedule == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(scheduleMapper.toDto(schedule));
    }

    @GetMapping("/{id}/events")
    public List<EventDto> getScheduleContents(@PathVariable Long id){
        var schedule = scheduleRepository.findById(id).orElse(null);
        if(schedule == null){
            return new ArrayList<>();
        }
        List<Event> events = eventRepository.findBySchedule(schedule);

        return events.stream().map(eventMapper::toDto).toList();
    }

    @GetMapping("/getByDate/{date}")
    public ResponseEntity<ScheduleDto> getScheduleByDate(@PathVariable @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date){
        var schedule = scheduleRepository.getByDate(date).orElse(null);
        if(schedule == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(scheduleMapper.toDto(schedule));
    }

    @Transactional
    @PostMapping("/template/{id}")
    public ResponseEntity<ScheduleDto> makeScheduleWithTemplate(@PathVariable(name = "id") Long templateId,
        @RequestBody ScheduleDto request,
        UriComponentsBuilder uriBuilder
    ){
        var template = scheduleTemplateRepository.findById(templateId).orElse(null);
        if(template == null){
            return ResponseEntity.notFound().build();
        }
        List<EventTemplate> eventTemplates = eventTemplateRepository.findByScheduleTemplate(template);

        var schedule = scheduleMapper.toEntity(request);
        scheduleRepository.save(schedule);

        List<Event> events = eventTemplates.stream().map(x -> eventMapper.toEvent(x, schedule)).toList();
        eventRepository.saveAll(events);

        var scheduleDto = scheduleMapper.toDto(schedule);

        var uri = uriBuilder.path("/schedules/{id}").buildAndExpand(scheduleDto.getId()).toUri();
        return ResponseEntity.created(uri).body(scheduleDto);
    }

    @PostMapping
    public ResponseEntity<ScheduleDto> createSchedule(
        @RequestBody ScheduleDto request,
        UriComponentsBuilder uriBuilder
    ){
        var schedule = scheduleMapper.toEntity(request);
        scheduleRepository.save(schedule);

        var scheduleDto = scheduleMapper.toDto(schedule);

        var uri = uriBuilder.path("/schedules/{id}").buildAndExpand(scheduleDto.getId()).toUri();
        return ResponseEntity.created(uri).body(scheduleDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable(name="id") Long id){
        var schedule = scheduleRepository.findById(id).orElse(null);
        if(schedule==null){
            return ResponseEntity.notFound().build();
        }

        scheduleRepository.delete(schedule);
        return ResponseEntity.noContent().build();
    }
}
