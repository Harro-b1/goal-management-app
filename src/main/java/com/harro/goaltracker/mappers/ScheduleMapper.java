package com.harro.goaltracker.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.harro.goaltracker.dtos.ScheduleDto;
import com.harro.goaltracker.entities.Schedule;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    ScheduleDto toDto(Schedule schedule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Schedule toEntity(ScheduleDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    void updateSchedule(ScheduleDto request, @MappingTarget Schedule schedule);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    void patchSchedule(ScheduleDto request, @MappingTarget Schedule schedule);
}
