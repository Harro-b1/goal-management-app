package com.harro.goaltracker.mappers;

import org.mapstruct.Mapper;

import com.harro.goaltracker.dtos.ScheduleDto;
import com.harro.goaltracker.entities.Schedule;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    ScheduleDto toDto(Schedule schedule);
    Schedule toEntity(ScheduleDto dto);
}
