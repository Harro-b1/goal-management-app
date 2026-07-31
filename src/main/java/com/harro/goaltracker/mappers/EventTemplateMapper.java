package com.harro.goaltracker.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.harro.goaltracker.dtos.EventTemplateDto;
import com.harro.goaltracker.entities.EventTemplate;

@Mapper(componentModel = "spring")
public interface EventTemplateMapper {
    @Mapping(source = "goal.id", target = "goal")
    @Mapping(source = "scheduleTemplate.id", target = "scheduleTemplate")
    EventTemplateDto toDto(EventTemplate eventTemplate);
    @Mapping(target = "goal", ignore = true)
    @Mapping(target = "scheduleTemplate", ignore = true)
    EventTemplate toEntity(EventTemplateDto dto);

    @Mapping(target = "goal", ignore = true)
    EventTemplate stripGoal(EventTemplate eventTemplate);
}
