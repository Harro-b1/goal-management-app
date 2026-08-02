package com.harro.goaltracker.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.harro.goaltracker.dtos.GoalDto;
import com.harro.goaltracker.entities.Goal;

@Mapper(componentModel = "spring")
public interface GoalMapper {
    @Mapping(source = "category.id", target = "category")
    GoalDto toDto(Goal goal);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    Goal toEntity(GoalDto dto);

    @Mapping(target = "category", ignore = true)
    Goal stripCategory(Goal goal);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateGoal(GoalDto request, @MappingTarget Goal goal);
}
