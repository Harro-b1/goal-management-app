package com.harro.goaltracker.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.harro.goaltracker.dtos.GoalDto;
import com.harro.goaltracker.entities.Goal;

@Mapper(componentModel = "spring")
public interface GoalMapper {
    @Mapping(source = "type.id", target = "type")
    GoalDto toDto(Goal goal);
}
