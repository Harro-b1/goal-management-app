package com.harro.goaltracker.dtos;

import java.time.LocalDate;

import com.harro.goaltracker.enums.PriorityLevel;

import lombok.Data;

@Data
public class GoalDto {
    private Long id;
    private String name;
    private String description;
    private boolean completed;
    private Long category;
    private PriorityLevel priority;
    private LocalDate finishByDate;
}
