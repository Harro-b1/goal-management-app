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
import com.harro.goaltracker.mappers.EventTemplateMapper;
import com.harro.goaltracker.services.crud.EventTemplateService;

import lombok.AllArgsConstructor;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/event-templates")
public class EventTemplateController {
    private final EventTemplateService eventTemplateService;
    private final EventTemplateMapper eventTemplateMapper;

    @GetMapping
    public List<EventTemplateDto> getAllEventTemplates(){
        return eventTemplateService.getAllEventTemplates().stream().map(eventTemplateMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventTemplateDto> getEventTemplate(@PathVariable Long id){
        return eventTemplateService.getEventTemplate(id)
            .map(eventTemplate -> ResponseEntity.ok(eventTemplateMapper.toDto(eventTemplate)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EventTemplateDto> createEventTemplate(
        @RequestBody EventTemplateDto request,
        UriComponentsBuilder uriBuilder
    ){
        var eventTemplate = eventTemplateService.createEventTemplate(request);
        var eventTemplateDto = eventTemplateMapper.toDto(eventTemplate);

        var uri = uriBuilder.path("/event-templates/{id}").buildAndExpand(eventTemplateDto.getId()).toUri();
        return ResponseEntity.created(uri).body(eventTemplateDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventTemplateDto> updateEventTemplate(
        @PathVariable(name="id") Long id,
        @RequestBody EventTemplateDto request
    ){
        return eventTemplateService.updateEventTemplate(id, request)
            .map(eventTemplate -> ResponseEntity.ok(eventTemplateMapper.toDto(eventTemplate)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EventTemplateDto> patchEventTemplate(
        @PathVariable(name="id") Long id,
        @RequestBody EventTemplateDto request
    ){
        return eventTemplateService.patchEventTemplate(id, request)
            .map(eventTemplate -> ResponseEntity.ok(eventTemplateMapper.toDto(eventTemplate)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventTemplate(@PathVariable(name="id") Long id){
        if(!eventTemplateService.deleteEventTemplate(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
