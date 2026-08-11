package com.harro.goaltracker.services.crud;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.harro.goaltracker.dtos.EventDto;
import com.harro.goaltracker.dtos.ScheduleDto;
import com.harro.goaltracker.entities.Event;
import com.harro.goaltracker.entities.Schedule;
import com.harro.goaltracker.mappers.EventMapper;
import com.harro.goaltracker.mappers.ScheduleMapper;
import com.harro.goaltracker.repositories.ScheduleRepository;
import com.harro.goaltracker.types.TimeSlot;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final EventService eventService;
    private final ScheduleTemplateService scheduleTemplateService;
    private final EventTemplateService eventTemplateService;
    private final EventMapper eventMapper;

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Optional<Schedule> getSchedule(Long id) {
        return scheduleRepository.findById(id);
    }

    public Optional<Schedule> getScheduleByDate(LocalDate date) {
        return scheduleRepository.getByDate(date);
    }

    public List<Event> getScheduleContents(Long id) {
        return getSchedule(id)
                .map(eventService::findBySchedule)
                .orElseGet(List::of);
    }

    public Schedule createSchedule(ScheduleDto request) {
        var schedule = scheduleMapper.toEntity(request);
        return scheduleRepository.save(schedule);
    }

    public Optional<Schedule> updateSchedule(Long id, ScheduleDto request) {
        return scheduleRepository.findById(id).map(schedule -> {
            scheduleMapper.updateSchedule(request, schedule);
            return scheduleRepository.saveAndFlush(schedule);
        });
    }

    public Optional<Schedule> patchSchedule(Long id, ScheduleDto request) {
        return scheduleRepository.findById(id).map(schedule -> {
            scheduleMapper.patchSchedule(request, schedule);
            return scheduleRepository.saveAndFlush(schedule);
        });
    }

    public boolean deleteSchedule(Long id) {
        var schedule = scheduleRepository.findById(id).orElse(null);
        if (schedule == null) {
            return false;
        }

        scheduleRepository.delete(schedule);
        return true;
    }

    public Optional<List<Event>> generateSchedule(Long id, LocalTime startTime, LocalTime endTime){
        var schedule = scheduleRepository.findById(id).orElse(null);
        if(schedule == null){
            return Optional.empty();
        }

        List<Event> events = getScheduleContents(id);
        List<TimeSlot> timeSlots = events.stream().map(eventMapper::toTimeSlot).toList();
        List<TimeSlot> freeTimeSlots = TimeSlot.getFreeTimeSlots(timeSlots, startTime, endTime);

        return null;
    }

    @Transactional
    public Optional<List<Event>> addEvents(Long id, List<EventDto> eventRequests){
        if(!scheduleRepository.existsById(id)){
            return Optional.empty();
        }
        
        List<Event> events = new ArrayList<>();
        for(var e : eventRequests){
            e.setSchedule(id);
            events.add(eventService.createEvent(e));
        }
        return Optional.of(events);
    }

    @Transactional
    public Optional<Schedule> makeScheduleWithTemplate(Long templateId, ScheduleDto request) {
        return scheduleTemplateService.getScheduleTemplate(templateId).map(template -> {
            var eventTemplates = eventTemplateService.findByScheduleTemplate(template);

            var schedule = scheduleMapper.toEntity(request);
            scheduleRepository.save(schedule);

            eventService.createEventsFromTemplates(eventTemplates, schedule);

            return schedule;
        });
    }
}
