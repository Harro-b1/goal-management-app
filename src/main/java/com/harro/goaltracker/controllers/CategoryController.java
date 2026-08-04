package com.harro.goaltracker.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.harro.goaltracker.dtos.CategoryDto;
import com.harro.goaltracker.mappers.CategoryMapper;
import com.harro.goaltracker.services.CategoryService;

import lombok.AllArgsConstructor;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public List<CategoryDto> getAllCategories(){
        return categoryService.getAllCategories().stream().map(categoryMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long id){
        return categoryService.getCategory(id)
            .map(category -> ResponseEntity.ok(categoryMapper.toDto(category)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
        @RequestBody CategoryDto request,
        UriComponentsBuilder uriBuilder
    ){
        var category = categoryService.createCategory(request);
        var categoryDto = categoryMapper.toDto(category);

        var uri = uriBuilder.path("/categories/{id}").buildAndExpand(categoryDto.getId()).toUri();
        return ResponseEntity.created(uri).body(categoryDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
        @PathVariable (name="id") Long id,
        @RequestBody CategoryDto request
    ){
        return categoryService.updateCategory(id, request)
            .map(category -> ResponseEntity.ok(categoryMapper.toDto(category)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryDto> patchCategory(
        @PathVariable (name="id") Long id,
        @RequestBody CategoryDto request
    ){
        return categoryService.patchCategory(id, request)
            .map(category -> ResponseEntity.ok(categoryMapper.toDto(category)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable(name="id") Long id){
        if(!categoryService.deleteCategory(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
