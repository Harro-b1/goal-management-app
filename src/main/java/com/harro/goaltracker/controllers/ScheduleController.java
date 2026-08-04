package com.harro.goaltracker.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.harro.goaltracker.dtos.ScheduleDto;
import com.harro.goaltracker.mappers.EventMapper;
import com.harro.goaltracker.mappers.ScheduleMapper;
import com.harro.goaltracker.services.OllamaChatService;
import com.harro.goaltracker.services.ScheduleService;

import lombok.AllArgsConstructor;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/schedules")
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final ScheduleMapper scheduleMapper;
    private final EventMapper eventMapper;
    private final OllamaChatService ollamaChatService;

    @GetMapping
    public List<ScheduleDto> getAllSchedules(){
        return scheduleService.getAllSchedules().stream().map(scheduleMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDto> getSchedule(@PathVariable Long id){
        return scheduleService.getSchedule(id)
            .map(schedule -> ResponseEntity.ok(scheduleMapper.toDto(schedule)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ScheduleDto> createSchedule(
        @RequestBody ScheduleDto request,
        UriComponentsBuilder uriBuilder
    ){
        var schedule = scheduleService.createSchedule(request);
        var scheduleDto = scheduleMapper.toDto(schedule);

        var uri = uriBuilder.path("/schedules/{id}").buildAndExpand(scheduleDto.getId()).toUri();
        return ResponseEntity.created(uri).body(scheduleDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleDto> updateSchedule(
        @PathVariable(name = "id") Long id,
        @RequestBody ScheduleDto request
    ){
        return scheduleService.updateSchedule(id, request)
            .map(schedule -> ResponseEntity.ok(scheduleMapper.toDto(schedule)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ScheduleDto> patchSchedule(
        @PathVariable(name = "id") Long id,
        @RequestBody ScheduleDto request
    ){
        return scheduleService.patchSchedule(id, request)
            .map(schedule -> ResponseEntity.ok(scheduleMapper.toDto(schedule)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable(name="id") Long id){
        if(!scheduleService.deleteSchedule(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/events")
    public List<EventDto> getScheduleContents(@PathVariable Long id){
        return scheduleService.getScheduleContents(id).stream().map(eventMapper::toDto).toList();
    }

    @GetMapping("/getByDate/{date}")
    public ResponseEntity<ScheduleDto> getScheduleByDate(@PathVariable @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date){
        return scheduleService.getScheduleByDate(date)
            .map(schedule -> ResponseEntity.ok(scheduleMapper.toDto(schedule)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/chat/{message}")
    public String chat(@PathVariable(name="message") String message){
        return ollamaChatService.chat(message);
    }

    @PostMapping("/template/{id}")
    public ResponseEntity<ScheduleDto> makeScheduleWithTemplate(@PathVariable(name = "id") Long templateId,
        @RequestBody ScheduleDto request,
        UriComponentsBuilder uriBuilder
    ){
        return scheduleService.makeScheduleWithTemplate(templateId, request)
            .map(schedule -> {
                var scheduleDto = scheduleMapper.toDto(schedule);
                var uri = uriBuilder.path("/schedules/{id}").buildAndExpand(scheduleDto.getId()).toUri();
                return ResponseEntity.created(uri).body(scheduleDto);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
