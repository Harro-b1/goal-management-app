package com.harro.goaltracker.controllers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
            .andExpect(status().isBadRequest())
            .andExpect(content().string("schedule cannot be null"));
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

    // Regression test: updateEvent used to mutate the managed entity via the mapper
    // before validating schedule/goal, so an invalid schedule still leaked the name
    // change to the database despite the request being rejected with 422.
    @Test
    void updateEvent_withInvalidSchedule_doesNotPersistPartialUpdate() throws Exception {
        EventDto request = new EventDto();
        request.setSchedule(9999L);
        request.setName("Mutated Name Should Not Persist");
        request.setStartTime(LocalTime.of(23, 0));
        request.setEndTime(LocalTime.of(23, 30));

        mockMvc.perform(put("/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422));

        mockMvc.perform(get("/events/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Morning run"));
    }

    @Test
    void updateEvent_withInvalidGoal_returns422() throws Exception {
        EventDto request = new EventDto();
        request.setSchedule(1L);
        request.setGoal(9999L);
        request.setName("Mutated Name Should Not Persist");
        request.setStartTime(LocalTime.of(23, 0));
        request.setEndTime(LocalTime.of(23, 30));

        mockMvc.perform(put("/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422))
            .andExpect(content().string("Invalid goal reference id"));

        mockMvc.perform(get("/events/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Morning run"));
    }

    @Test
    void updateEvent_existingId_updatesAndReturnsOk() throws Exception {
        EventDto request = new EventDto();
        request.setSchedule(2L);
        request.setGoal(3L);
        request.setName("Updated event name");
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(8, 45));

        mockMvc.perform(put("/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated event name"))
            .andExpect(jsonPath("$.schedule").value(2))
            .andExpect(jsonPath("$.goal").value(3));
    }

    @Test
    void updateEvent_missingId_returns404() throws Exception {
        EventDto request = new EventDto();
        request.setSchedule(1L);
        request.setName("Doesn't matter");
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(8, 30));

        mockMvc.perform(put("/events/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateEvent_withoutSchedule_returns400WithMessage() throws Exception {
        EventDto request = new EventDto();
        request.setName("Doesn't matter");
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(8, 30));

        mockMvc.perform(put("/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("schedule cannot be null"));
    }

    // Regression test: PUT sending goal:null used to be silently ignored (treated as
    // "leave unchanged") instead of clearing the relation - see context.md bug #5.
    @Test
    void updateEvent_withNullGoal_clearsExistingGoal() throws Exception {
        EventDto request = new EventDto();
        request.setSchedule(1L);
        request.setGoal(null);
        request.setName("Morning run");
        request.setStartTime(LocalTime.of(7, 0));
        request.setEndTime(LocalTime.of(7, 30));

        mockMvc.perform(put("/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.goal").value(nullValue()));
    }

    @Test
    void deleteEvent_existingId_removesEventAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/events/1"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/events/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteEvent_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/events/9999"))
            .andExpect(status().isNotFound());
    }

    // Exercises NullAssignmentException: events.name has no application-level guard,
    // so a null name reaches the DB's NOT NULL constraint.
    @Test
    void createEvent_withNullName_returns400WithMessage() throws Exception {
        EventDto request = new EventDto();
        request.setSchedule(1L);
        request.setName(null);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("name cannot be null"));
    }
}
