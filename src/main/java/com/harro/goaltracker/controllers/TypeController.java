package com.harro.goaltracker.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

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

    @GetMapping("/{id}")
    public ResponseEntity<TypeDto> getType(@PathVariable Long id){
        var type = typeRepository.findById(id).orElse(null);

        if(type == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(typeMapper.toDto(type));
    }

    @PostMapping
    public ResponseEntity<TypeDto> createType(
        @RequestBody TypeDto request,
        UriComponentsBuilder uriBuilder
    ){
        var type = typeMapper.toEntity(request);
        type.setId(null);
        typeRepository.save(type);

        var typeDto = typeMapper.toDto(type);

        var uri = uriBuilder.path("/types/{id}").buildAndExpand(typeDto.getId()).toUri();
        return ResponseEntity.created(uri).body(typeDto);
    }
}
