package com.harro.goaltracker.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.harro.goaltracker.dtos.CategoryDto;
import com.harro.goaltracker.entities.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category category);
    Category toEntity(CategoryDto dto);

    @Mapping(target = "id", ignore = true)
    void updateCategory(CategoryDto request, @MappingTarget Category category);
}
