package com.harro.goaltracker.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.harro.goaltracker.dtos.CategoryDto;
import com.harro.goaltracker.entities.Category;
import com.harro.goaltracker.entities.Goal;
import com.harro.goaltracker.mappers.CategoryMapper;
import com.harro.goaltracker.mappers.GoalMapper;
import com.harro.goaltracker.repositories.CategoryRepository;
import com.harro.goaltracker.repositories.GoalRepository;

import lombok.AllArgsConstructor;

@RestController
@CrossOrigin
@AllArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;

    @GetMapping
    public List<CategoryDto> getAllCategories(){
        List<Category> categories = categoryRepository.findAll();

        return categories.stream().map(categoryMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long id){
        var category = categoryRepository.findById(id).orElse(null);

        if(category == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(categoryMapper.toDto(category));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
        @RequestBody CategoryDto request,
        UriComponentsBuilder uriBuilder
    ){
        var category = categoryMapper.toEntity(request);
        category.setId(null);
        categoryRepository.save(category);

        var categoryDto = categoryMapper.toDto(category);

        var uri = uriBuilder.path("/categories/{id}").buildAndExpand(categoryDto.getId()).toUri();
        return ResponseEntity.created(uri).body(categoryDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable(name="id") Long id){
        var category = categoryRepository.findById(id).orElse(null);
        if(category == null){
            return ResponseEntity.notFound().build();
        }

        var goalsWithCategory = goalRepository.findAllByCategory(category);
        var strippedGoals = goalsWithCategory.stream().map(goalMapper::stripCategory).toList();
        goalRepository.saveAll(strippedGoals);

        categoryRepository.delete(category);

        return ResponseEntity.noContent().build();
    }
}
