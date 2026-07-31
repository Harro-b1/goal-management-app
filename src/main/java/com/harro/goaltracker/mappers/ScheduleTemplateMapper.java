package com.harro.goaltracker.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.harro.goaltracker.dtos.ScheduleTemplateDto;
import com.harro.goaltracker.entities.ScheduleTemplate;

@Mapper(componentModel = "spring")
public interface ScheduleTemplateMapper {
    ScheduleTemplateDto toDto(ScheduleTemplate scheduleTemplate);
    @Mapping(target = "id", ignore = true)
    ScheduleTemplate toEntity(ScheduleTemplateDto dto);
}
