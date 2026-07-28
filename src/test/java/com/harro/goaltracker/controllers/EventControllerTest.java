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
import com.harro.goaltracker.dtos.EventDto;

class EventControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllEvents_returnsSeededEvents() throws Exception {
        mockMvc.perform(get("/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))));
    }

    @Test
    void getEvent_existingId_returnsEvent() throws Exception {
        mockMvc.perform(get("/events/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Morning run"));
    }

    @Test
    void getEvent_missingId_returns404() throws Exception {
        mockMvc.perform(get("/events/9999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createEvent_withGoalAndSchedule_persistsAndReturnsCreated() throws Exception {
        EventDto request = new EventDto();
        request.setSchedule(1L);
        request.setGoal(1L);
        request.setName("Evening walk");
        request.setStartTime(LocalTime.of(18, 0));
        request.setEndTime(LocalTime.of(18, 30));

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.goal").value(1))
            .andExpect(jsonPath("$.schedule").value(1));
    }

    // Regression test: EventMapper#toEntity used to build a placeholder Goal(id=null)
    // even when no goal was supplied, and cascade PERSIST tried to insert it, blowing
    // up on the goals.name NOT NULL constraint.
    @Test
    void createEvent_withoutGoal_persistsWithNullGoal() throws Exception {
        EventDto request = new EventDto();
        request.setSchedule(1L);
        request.setName("Standalone event");
        request.setStartTime(LocalTime.of(12, 0));
        request.setEndTime(LocalTime.of(12, 30));

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.goal").value(nullValue()));
    }

    @Test
    void createEvent_withoutSchedule_returns400() throws Exception {
        EventDto request = new EventDto();
        request.setName("No schedule event");
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_withInvalidSchedule_returns422() throws Exception {
        EventDto request = new EventDto();
        request.setSchedule(9999L);
        request.setName("Bad schedule event");
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422));
    }

    @Test
    void createEvent_withInvalidGoal_returns422() throws Exception {
        EventDto request = new EventDto();
        request.setSchedule(1L);
        request.setGoal(9999L);
        request.setName("Bad goal event");
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422));
    }
}
