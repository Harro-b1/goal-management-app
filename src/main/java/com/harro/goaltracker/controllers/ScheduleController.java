package com.harro.goaltracker.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.harro.goaltracker.dtos.ScheduleDto;
import com.harro.goaltracker.entities.Schedule;
import com.harro.goaltracker.mappers.ScheduleMapper;
import com.harro.goaltracker.repositories.ScheduleRepository;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/schedules")
public class ScheduleController {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

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

    @PostMapping
    public ResponseEntity<ScheduleDto> createSchedule(
        @RequestBody ScheduleDto request,
        UriComponentsBuilder uriBuilder
    ){
        var schedule = scheduleMapper.toEntity(request);
        schedule.setId(null);
        scheduleRepository.save(schedule);

        var scheduleDto = scheduleMapper.toDto(schedule);

        var uri = uriBuilder.path("/schedules/{id}").buildAndExpand(scheduleDto.getId()).toUri();
        return ResponseEntity.created(uri).body(scheduleDto);
    }
}
