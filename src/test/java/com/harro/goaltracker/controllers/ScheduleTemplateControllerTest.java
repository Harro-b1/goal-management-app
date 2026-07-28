package com.harro.goaltracker.controllers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
