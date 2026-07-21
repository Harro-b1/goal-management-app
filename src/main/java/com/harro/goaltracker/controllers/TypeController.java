package com.harro.goaltracker.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harro.goaltracker.Repositories.TypeRepository;
import com.harro.goaltracker.dtos.TypeDto;
import com.harro.goaltracker.entities.Type;
import com.harro.goaltracker.mappers.TypeMapper;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/types")
public class TypeController {
    private final TypeRepository typeRepository;
    private final TypeMapper typeMapper;

    @GetMapping
    public List<TypeDto> getAllTypes(){
        List<Type> types = typeRepository.findAll();

        return types.stream().map(typeMapper::toDto).toList();
    }
}
