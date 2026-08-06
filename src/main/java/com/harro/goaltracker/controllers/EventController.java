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

import com.harro.goaltracker.dtos.EventDto;
import com.harro.goaltracker.mappers.EventMapper;
import com.harro.goaltracker.services.crud.EventService;

import lombok.AllArgsConstructor;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;
    private final EventMapper eventMapper;

    @GetMapping
    public List<EventDto> getAllEvents(){
        return eventService.getAllEvents().stream().map(eventMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long id){
        return eventService.getEvent(id)
            .map(event -> ResponseEntity.ok(eventMapper.toDto(event)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EventDto> createEvent(
        @RequestBody EventDto request,
        UriComponentsBuilder uriBuilder
    ){
        var event = eventService.createEvent(request);
        var eventDto = eventMapper.toDto(event);

        var uri = uriBuilder.path("/events/{id}").buildAndExpand(eventDto.getId()).toUri();
        return ResponseEntity.created(uri).body(eventDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDto> updateEvent(
        @PathVariable(name="id") Long id,
        @RequestBody EventDto request
    ){
        return eventService.updateEvent(id, request)
            .map(event -> ResponseEntity.ok(eventMapper.toDto(event)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EventDto> patchEvent(
        @PathVariable(name="id") Long id,
        @RequestBody EventDto request
    ){
        return eventService.patchEvent(id, request)
            .map(event -> ResponseEntity.ok(eventMapper.toDto(event)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable (name="id") Long id){
        if(!eventService.deleteEvent(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
