package com.harro.goaltracker.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.harro.goaltracker.dtos.GoalDto;
import com.harro.goaltracker.entities.Category;
import com.harro.goaltracker.entities.Goal;
import com.harro.goaltracker.exceptions.InvalidReferenceException;
import com.harro.goaltracker.mappers.GoalMapper;
import com.harro.goaltracker.repositories.CategoryRepository;
import com.harro.goaltracker.repositories.GoalRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GoalService {
    private static final Sort GOALS_DEFAULT_SORT = Sort.by("priority").descending()
            .and(Sort.by(Sort.Order.asc("finishByDate").nullsLast()));

    private final GoalRepository goalRepository;
    private final CategoryRepository categoryRepository;
    private final GoalMapper goalMapper;
    private final EventService eventService;
    private final EventTemplateService eventTemplateService;

    public List<Goal> getAllGoals() {
        return goalRepository.findAll(GOALS_DEFAULT_SORT);
    }

    public Optional<Goal> getGoal(Long id) {
        return goalRepository.findById(id);
    }

    public List<Goal> searchGoals(String query) {
        return goalRepository.searchGoalsByString(query);
    }

    public Goal createGoal(GoalDto request) {
        var goal = goalMapper.toEntity(request);
        goal.setCategory(resolveCategory(request.getCategory()));
        return goalRepository.save(goal);
    }

    public Optional<Goal> updateGoal(Long id, GoalDto request) {
        return goalRepository.findById(id).map(goal -> {
            goal.setCategory(resolveCategory(request.getCategory()));
            goalMapper.updateGoal(request, goal);
            return goalRepository.save(goal);
        });
    }

    public Optional<Goal> patchGoal(Long id, GoalDto request) {
        return goalRepository.findById(id).map(goal -> {
            if (request.getCategory() != null) {
                goal.setCategory(resolveCategory(request.getCategory()));
            }
            goalMapper.patchGoal(request, goal);
            return goalRepository.save(goal);
        });
    }

    @Transactional
    public boolean deleteGoal(Long id) {
        var goal = goalRepository.findById(id).orElse(null);
        if (goal == null) {
            return false;
        }

        eventService.stripGoalFromEvents(goal);
        eventTemplateService.stripGoalFromEventTemplates(goal);
        goalRepository.delete(goal);
        return true;
    }

    public void stripCategoryFromGoals(Category category) {
        var goals = goalRepository.findAllByCategory(category);
        var strippedGoals = goals.stream().map(goalMapper::stripCategory).toList();
        goalRepository.saveAll(strippedGoals);
    }

    public Optional<Goal> completeGoal(Long id) {
        return setCompleted(id, true);
    }

    public Optional<Goal> uncompleteGoal(Long id) {
        return setCompleted(id, false);
    }

    private Optional<Goal> setCompleted(Long id, boolean completed) {
        return goalRepository.findById(id).map(goal -> {
            goal.setCompleted(completed);
            return goalRepository.save(goal);
        });
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new InvalidReferenceException("category"));
    }
}
