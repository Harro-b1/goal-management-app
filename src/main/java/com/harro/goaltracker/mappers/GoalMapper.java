package com.harro.goaltracker.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.harro.goaltracker.dtos.GoalDto;
import com.harro.goaltracker.entities.Goal;

@Mapper(componentModel = "spring")
public interface GoalMapper {
    @Mapping(source = "category.id", target = "category")
    GoalDto toDto(Goal goal);
    @Mapping(source = "category", target = "category.id")
    Goal toEntity(GoalDto dto);
}
