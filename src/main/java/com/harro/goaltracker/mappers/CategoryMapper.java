package com.harro.goaltracker.mappers;

import org.mapstruct.Mapper;

import com.harro.goaltracker.dtos.CategoryDto;
import com.harro.goaltracker.entities.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category category);
    Category toEntity(CategoryDto dto);
}
