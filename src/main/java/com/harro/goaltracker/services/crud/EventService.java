package com.harro.goaltracker.services.crud;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.harro.goaltracker.dtos.EventDto;
import com.harro.goaltracker.entities.Event;
import com.harro.goaltracker.entities.EventTemplate;
import com.harro.goaltracker.entities.Goal;
import com.harro.goaltracker.entities.Schedule;
import com.harro.goaltracker.exceptions.InvalidReferenceException;
import com.harro.goaltracker.exceptions.NullAssignmentException;
import com.harro.goaltracker.mappers.EventMapper;
import com.harro.goaltracker.repositories.EventRepository;
import com.harro.goaltracker.repositories.GoalRepository;
import com.harro.goaltracker.repositories.ScheduleRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final GoalRepository goalRepository;
    private final ScheduleRepository scheduleRepository;
    private final EventMapper eventMapper;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> getEvent(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> findBySchedule(Schedule schedule) {
        return eventRepository.findBySchedule(schedule);
    }

    public Event createEvent(EventDto request) {
        var event = eventMapper.toEntity(request);

        event.setSchedule(resolveSchedule(request.getSchedule()));
        event.setGoal(resolveGoal(request.getGoal()));

        return eventRepository.save(event);
    }

    public Optional<Event> updateEvent(Long id, EventDto request) {
        return eventRepository.findById(id).map(event -> {
            event.setSchedule(resolveSchedule(request.getSchedule()));
            event.setGoal(resolveGoal(request.getGoal()));

            eventMapper.updateEvent(request, event);
            return eventRepository.save(event);
        });
    }

    public Optional<Event> patchEvent(Long id, EventDto request) {
        return eventRepository.findById(id).map(event -> {
            if (request.getSchedule() != null) {
                event.setSchedule(resolveSchedule(request.getSchedule()));
            }
            if (request.getGoal() != null) {
                event.setGoal(resolveGoal(request.getGoal()));
            }

            eventMapper.patchEvent(request, event);
            return eventRepository.save(event);
        });
    }

    public boolean deleteEvent(Long id) {
        var event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            return false;
        }

        eventRepository.delete(event);
        return true;
    }

    public List<Event> createEventsFromTemplates(List<EventTemplate> eventTemplates, Schedule schedule) {
        var events = eventTemplates.stream().map(template -> eventMapper.toEvent(template, schedule)).toList();
        return eventRepository.saveAll(events);
    }

    public void stripGoalFromEvents(Goal goal) {
        var events = eventRepository.findByGoal(goal);
        var strippedEvents = events.stream().map(eventMapper::stripGoal).toList();
        eventRepository.saveAll(strippedEvents);
    }

    private Schedule resolveSchedule(Long scheduleId) {
        if (scheduleId == null) {
            throw new NullAssignmentException("schedule");
        }
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new InvalidReferenceException("schedule"));
    }

    private Goal resolveGoal(Long goalId) {
        if (goalId == null) {
            return null;
        }
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new InvalidReferenceException("goal"));
    }
}
