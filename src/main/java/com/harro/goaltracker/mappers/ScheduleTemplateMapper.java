package com.harro.goaltracker.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.harro.goaltracker.dtos.ScheduleTemplateDto;
import com.harro.goaltracker.entities.ScheduleTemplate;

@Mapper(componentModel = "spring")
public interface ScheduleTemplateMapper {
    ScheduleTemplateDto toDto(ScheduleTemplate scheduleTemplate);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventTemplates", ignore = true)
    ScheduleTemplate toEntity(ScheduleTemplateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventTemplates", ignore = true)
    void updateScheduleTemplate(ScheduleTemplateDto request, @MappingTarget ScheduleTemplate scheduleTemplate);
}
