package com.harro.goaltracker.dtos;

import java.time.LocalTime;

import lombok.Data;

@Data
public class EventDto {
    private Long id;
    private Long goal;
    private Long schedule;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
}
