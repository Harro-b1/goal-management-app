package com.harro.goaltracker.controllers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.harro.goaltracker.AbstractIntegrationTest;
import com.harro.goaltracker.dtos.ScheduleTemplateDto;

class ScheduleTemplateControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllScheduleTemplates_returnsSeededScheduleTemplates() throws Exception {
        mockMvc.perform(get("/schedule-templates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void getScheduleTemplate_existingId_returnsScheduleTemplate() throws Exception {
        mockMvc.perform(get("/schedule-templates/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Weekday Routine"));
    }

    @Test
    void getScheduleTemplate_missingId_returns404() throws Exception {
        mockMvc.perform(get("/schedule-templates/9999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createScheduleTemplate_persistsAndReturnsCreated() throws Exception {
        ScheduleTemplateDto request = new ScheduleTemplateDto();
        request.setName("Holiday Routine");

        mockMvc.perform(post("/schedule-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.name").value("Holiday Routine"));
    }

    @Test
    void updateScheduleTemplate_existingId_updatesAndReturnsOk() throws Exception {
        ScheduleTemplateDto request = new ScheduleTemplateDto();
        request.setName("Updated Routine");

        mockMvc.perform(put("/schedule-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Routine"));
    }

    @Test
    void updateScheduleTemplate_missingId_returns404() throws Exception {
        ScheduleTemplateDto request = new ScheduleTemplateDto();
        request.setName("Doesn't matter");

        mockMvc.perform(put("/schedule-templates/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    // Exercises DuplicateDataException via the schedule_templates.name UNIQUE constraint.
    @Test
    void updateScheduleTemplate_duplicateName_returns409WithMessage() throws Exception {
        ScheduleTemplateDto request = new ScheduleTemplateDto();
        request.setName("Weekend Routine");

        mockMvc.perform(put("/schedule-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(content().string("Duplicate value for name"));
    }

    @Test
    void createScheduleTemplate_duplicateName_returns409WithMessage() throws Exception {
        ScheduleTemplateDto request = new ScheduleTemplateDto();
        request.setName("Weekday Routine");

        mockMvc.perform(post("/schedule-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(content().string("Duplicate value for name"));
    }

    // Exercises NullAssignmentException via the schedule_templates.name NOT NULL
    // constraint - there's no application-level guard for this field.
    @Test
    void createScheduleTemplate_withNullName_returns400WithMessage() throws Exception {
        ScheduleTemplateDto request = new ScheduleTemplateDto();
        request.setName(null);

        mockMvc.perform(post("/schedule-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("name cannot be null"));
    }

    @Test
    void deleteScheduleTemplate_existingId_removesScheduleTemplateAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/schedule-templates/2"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/schedule-templates/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteScheduleTemplate_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/schedule-templates/9999"))
            .andExpect(status().isNotFound());
    }

    // Regression test: deleting a ScheduleTemplate cascades REMOVE to dependent
    // EventTemplates (the FK is NOT NULL, so there's no valid "unlinked" state) - see
    // context.md domain model notes. Schedule template 1 owns event templates 1 and 2.
    @Test
    void deleteScheduleTemplate_cascadesDeleteToEventTemplates() throws Exception {
        mockMvc.perform(delete("/schedule-templates/1"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/event-templates/1"))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/event-templates/2"))
            .andExpect(status().isNotFound());
    }
}
