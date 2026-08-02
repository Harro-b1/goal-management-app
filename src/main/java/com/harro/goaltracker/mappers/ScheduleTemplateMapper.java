package com.harro.goaltracker.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

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

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventTemplates", ignore = true)
    void patchScheduleTemplate(ScheduleTemplateDto request, @MappingTarget ScheduleTemplate scheduleTemplate);
}
