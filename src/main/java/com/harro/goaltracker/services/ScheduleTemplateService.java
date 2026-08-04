package com.harro.goaltracker.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.harro.goaltracker.dtos.ScheduleTemplateDto;
import com.harro.goaltracker.entities.EventTemplate;
import com.harro.goaltracker.entities.ScheduleTemplate;
import com.harro.goaltracker.mappers.ScheduleTemplateMapper;
import com.harro.goaltracker.repositories.ScheduleTemplateRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ScheduleTemplateService {
    private final ScheduleTemplateRepository scheduleTemplateRepository;
    private final ScheduleTemplateMapper scheduleTemplateMapper;
    private final EventTemplateService eventTemplateService;

    public List<ScheduleTemplate> getAllScheduleTemplates() {
        return scheduleTemplateRepository.findAll();
    }

    public Optional<ScheduleTemplate> getScheduleTemplate(Long id) {
        return scheduleTemplateRepository.findById(id);
    }

    public List<EventTemplate> getScheduleTemplateContents(Long id) {
        return getScheduleTemplate(id)
                .map(eventTemplateService::findByScheduleTemplate)
                .orElseGet(List::of);
    }

    public ScheduleTemplate createScheduleTemplate(ScheduleTemplateDto request) {
        var scheduleTemplate = scheduleTemplateMapper.toEntity(request);
        return scheduleTemplateRepository.save(scheduleTemplate);
    }

    public Optional<ScheduleTemplate> updateScheduleTemplate(Long id, ScheduleTemplateDto request) {
        return scheduleTemplateRepository.findById(id).map(scheduleTemplate -> {
            scheduleTemplateMapper.updateScheduleTemplate(request, scheduleTemplate);
            return scheduleTemplateRepository.saveAndFlush(scheduleTemplate);
        });
    }

    public Optional<ScheduleTemplate> patchScheduleTemplate(Long id, ScheduleTemplateDto request) {
        return scheduleTemplateRepository.findById(id).map(scheduleTemplate -> {
            scheduleTemplateMapper.patchScheduleTemplate(request, scheduleTemplate);
            return scheduleTemplateRepository.saveAndFlush(scheduleTemplate);
        });
    }

    public boolean deleteScheduleTemplate(Long id) {
        var scheduleTemplate = scheduleTemplateRepository.findById(id).orElse(null);
        if (scheduleTemplate == null) {
            return false;
        }

        scheduleTemplateRepository.delete(scheduleTemplate);
        return true;
    }
}
