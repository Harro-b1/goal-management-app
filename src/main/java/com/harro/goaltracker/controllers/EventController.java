package com.harro.goaltracker.controllers;

import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.harro.goaltracker.dtos.EventDto;
import com.harro.goaltracker.entities.Event;
import com.harro.goaltracker.mappers.EventMapper;
import com.harro.goaltracker.repositories.EventRepository;
import com.harro.goaltracker.repositories.GoalRepository;
import com.harro.goaltracker.repositories.ScheduleRepository;

import lombok.AllArgsConstructor;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/events")
public class EventController {
    private final EventRepository eventRepository;
    private final GoalRepository goalRepository;
    private final ScheduleRepository scheduleRepository;
    private final EventMapper eventMapper;

    @GetMapping
    public List<EventDto> getAllEvents(){
        List<Event> events = eventRepository.findAll();

        return events.stream().map(eventMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long id){
        var event = eventRepository.findById(id).orElse(null);

        if(event == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(eventMapper.toDto(event));
    }

    @PostMapping
    public ResponseEntity<EventDto> createEvent(
        @RequestBody EventDto request,
        UriComponentsBuilder uriBuilder
    ){
        request.setId(null);
        var event = eventMapper.toEntity(request);

        if(request.getSchedule() == null){
            return ResponseEntity.badRequest().build();
        }

        var schedule = scheduleRepository.findById(request.getSchedule()).orElse(null);
        if(schedule == null){
            return new ResponseEntity<>(HttpStatusCode.valueOf(422));
        }
        event.setSchedule(schedule);

        if(request.getGoal() != null){
            var goal = goalRepository.findById(request.getGoal()).orElse(null);
            if(goal == null){
                return new ResponseEntity<>(HttpStatusCode.valueOf(422));
            }
            event.setGoal(goal);
        }

        eventRepository.save(event);

        var eventDto = eventMapper.toDto(event);

        var uri = uriBuilder.path("/events/{id}").buildAndExpand(eventDto.getId()).toUri();
        return ResponseEntity.created(uri).body(eventDto);
    }
}
