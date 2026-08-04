package com.harro.goaltracker.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.harro.goaltracker.dtos.CategoryDto;
import com.harro.goaltracker.entities.Category;
import com.harro.goaltracker.mappers.CategoryMapper;
import com.harro.goaltracker.repositories.CategoryRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final GoalService goalService;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategory(Long id) {
        return categoryRepository.findById(id);
    }

    public Category createCategory(CategoryDto request) {
        var category = categoryMapper.toEntity(request);
        category.setId(null);
        return categoryRepository.save(category);
    }

    public Optional<Category> updateCategory(Long id, CategoryDto request) {
        return categoryRepository.findById(id).map(category -> {
            categoryMapper.updateCategory(request, category);
            return categoryRepository.saveAndFlush(category);
        });
    }

    public Optional<Category> patchCategory(Long id, CategoryDto request) {
        return categoryRepository.findById(id).map(category -> {
            categoryMapper.patchCategory(request, category);
            return categoryRepository.saveAndFlush(category);
        });
    }

    @Transactional
    public boolean deleteCategory(Long id) {
        var category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            return false;
        }

        goalService.stripCategoryFromGoals(category);
        categoryRepository.delete(category);
        return true;
    }
}
