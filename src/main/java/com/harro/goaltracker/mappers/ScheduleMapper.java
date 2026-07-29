package com.harro.goaltracker.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.harro.goaltracker.dtos.ScheduleDto;
import com.harro.goaltracker.entities.Schedule;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    ScheduleDto toDto(Schedule schedule);
    @Mapping(target = "id", ignore = true)
    Schedule toEntity(ScheduleDto dto);
}
