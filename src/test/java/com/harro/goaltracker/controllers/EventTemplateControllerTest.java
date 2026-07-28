package com.harro.goaltracker.controllers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.harro.goaltracker.AbstractIntegrationTest;
import com.harro.goaltracker.dtos.EventTemplateDto;

class EventTemplateControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllEventTemplates_returnsSeededEventTemplates() throws Exception {
        mockMvc.perform(get("/event-templates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void getEventTemplate_existingId_returnsEventTemplate() throws Exception {
        mockMvc.perform(get("/event-templates/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Morning run"));
    }

    @Test
    void getEventTemplate_missingId_returns404() throws Exception {
        mockMvc.perform(get("/event-templates/9999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createEventTemplate_withGoalAndScheduleTemplate_persistsAndReturnsCreated() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setScheduleTemplate(1L);
        request.setGoal(1L);
        request.setName("Evening walk");
        request.setStartTime(LocalTime.of(18, 0));
        request.setEndTime(LocalTime.of(18, 30));

        mockMvc.perform(post("/event-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.goal").value(1))
            .andExpect(jsonPath("$.scheduleTemplate").value(1));
    }

    @Test
    void createEventTemplate_withoutGoal_persistsWithNullGoal() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setScheduleTemplate(1L);
        request.setName("Standalone template event");
        request.setStartTime(LocalTime.of(12, 0));
        request.setEndTime(LocalTime.of(12, 30));

        mockMvc.perform(post("/event-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.goal").value(nullValue()));
    }

    @Test
    void createEventTemplate_withoutScheduleTemplate_returns400() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setName("No schedule template event");
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        mockMvc.perform(post("/event-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createEventTemplate_withInvalidScheduleTemplate_returns422() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setScheduleTemplate(9999L);
        request.setName("Bad schedule template event");
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        mockMvc.perform(post("/event-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422));
    }

    @Test
    void createEventTemplate_withInvalidGoal_returns422() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setScheduleTemplate(1L);
        request.setGoal(9999L);
        request.setName("Bad goal event");
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        mockMvc.perform(post("/event-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422));
    }
}
