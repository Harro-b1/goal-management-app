package com.harro.goaltracker.dtos;

import java.time.LocalTime;

import lombok.Data;

@Data
public class EventTemplateDto {
    private Long id;
    private Long goal;
    private Long scheduleTemplate;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
}
