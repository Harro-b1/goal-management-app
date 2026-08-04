package com.harro.goaltracker.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.harro.goaltracker.dtos.EventTemplateDto;
import com.harro.goaltracker.entities.EventTemplate;
import com.harro.goaltracker.entities.Goal;
import com.harro.goaltracker.entities.ScheduleTemplate;
import com.harro.goaltracker.exceptions.InvalidReferenceException;
import com.harro.goaltracker.exceptions.NullAssignmentException;
import com.harro.goaltracker.mappers.EventTemplateMapper;
import com.harro.goaltracker.repositories.EventTemplateRepository;
import com.harro.goaltracker.repositories.GoalRepository;
import com.harro.goaltracker.repositories.ScheduleTemplateRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EventTemplateService {
    private final EventTemplateRepository eventTemplateRepository;
    private final GoalRepository goalRepository;
    private final ScheduleTemplateRepository scheduleTemplateRepository;
    private final EventTemplateMapper eventTemplateMapper;

    public List<EventTemplate> getAllEventTemplates() {
        return eventTemplateRepository.findAll();
    }

    public Optional<EventTemplate> getEventTemplate(Long id) {
        return eventTemplateRepository.findById(id);
    }

    public List<EventTemplate> findByScheduleTemplate(ScheduleTemplate scheduleTemplate) {
        return eventTemplateRepository.findByScheduleTemplate(scheduleTemplate);
    }

    public EventTemplate createEventTemplate(EventTemplateDto request) {
        var eventTemplate = eventTemplateMapper.toEntity(request);

        eventTemplate.setScheduleTemplate(resolveScheduleTemplate(request.getScheduleTemplate()));
        eventTemplate.setGoal(resolveGoal(request.getGoal()));

        return eventTemplateRepository.save(eventTemplate);
    }

    public Optional<EventTemplate> updateEventTemplate(Long id, EventTemplateDto request) {
        return eventTemplateRepository.findById(id).map(eventTemplate -> {
            eventTemplate.setScheduleTemplate(resolveScheduleTemplate(request.getScheduleTemplate()));
            eventTemplate.setGoal(resolveGoal(request.getGoal()));

            eventTemplateMapper.updateEventTemplate(request, eventTemplate);
            return eventTemplateRepository.save(eventTemplate);
        });
    }

    public Optional<EventTemplate> patchEventTemplate(Long id, EventTemplateDto request) {
        return eventTemplateRepository.findById(id).map(eventTemplate -> {
            if (request.getScheduleTemplate() != null) {
                eventTemplate.setScheduleTemplate(resolveScheduleTemplate(request.getScheduleTemplate()));
            }
            if (request.getGoal() != null) {
                eventTemplate.setGoal(resolveGoal(request.getGoal()));
            }

            eventTemplateMapper.patchEventTemplate(request, eventTemplate);
            return eventTemplateRepository.save(eventTemplate);
        });
    }

    public boolean deleteEventTemplate(Long id) {
        var eventTemplate = eventTemplateRepository.findById(id).orElse(null);
        if (eventTemplate == null) {
            return false;
        }

        eventTemplateRepository.delete(eventTemplate);
        return true;
    }

    public void stripGoalFromEventTemplates(Goal goal) {
        var eventTemplates = eventTemplateRepository.findByGoal(goal);
        var strippedEventTemplates = eventTemplates.stream().map(eventTemplateMapper::stripGoal).toList();
        eventTemplateRepository.saveAll(strippedEventTemplates);
    }

    private ScheduleTemplate resolveScheduleTemplate(Long scheduleTemplateId) {
        if (scheduleTemplateId == null) {
            throw new NullAssignmentException("scheduleTemplate");
        }
        return scheduleTemplateRepository.findById(scheduleTemplateId)
                .orElseThrow(() -> new InvalidReferenceException("scheduleTemplate"));
    }

    private Goal resolveGoal(Long goalId) {
        if (goalId == null) {
            return null;
        }
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new InvalidReferenceException("goal"));
    }
}
