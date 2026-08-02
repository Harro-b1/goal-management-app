package com.harro.goaltracker.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.harro.goaltracker.dtos.EventDto;
import com.harro.goaltracker.entities.Event;
import com.harro.goaltracker.entities.EventTemplate;
import com.harro.goaltracker.entities.Schedule;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(source = "goal.id", target = "goal")
    @Mapping(source = "schedule.id", target = "schedule")
    EventDto toDto(Event event);
    @Mapping(target = "goal", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    Event toEntity(EventDto dto);

    @Mapping(source = "schedule", target = "schedule")
    @Mapping(target = "id", ignore = true)
    Event toEvent(EventTemplate template, Schedule schedule);

    @Mapping(target="goal",ignore = true)
    Event stripGoal(Event event);

    @Mapping(target="goal",ignore=true)
    @Mapping(target="id",ignore=true)
    @Mapping(target="schedule",ignore=true)
    void updateEvent(EventDto request, @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target="goal",ignore=true)
    @Mapping(target="id",ignore=true)
    @Mapping(target="schedule",ignore=true)
    void patchEvent(EventDto request, @MappingTarget Event event);
}
