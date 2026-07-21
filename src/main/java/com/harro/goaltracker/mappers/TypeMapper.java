package com.harro.goaltracker.mappers;

import org.mapstruct.Mapper;

import com.harro.goaltracker.dtos.TypeDto;
import com.harro.goaltracker.entities.Type;;

@Mapper(componentModel = "spring")
public interface TypeMapper {
    TypeDto toDto(Type type);
}
