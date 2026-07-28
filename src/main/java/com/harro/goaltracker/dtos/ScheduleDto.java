package com.harro.goaltracker.dtos;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ScheduleDto {
    private Long id;
    private LocalDate date;
}
