package com.harro.goaltracker.controllers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.harro.goaltracker.AbstractIntegrationTest;
import com.harro.goaltracker.dtos.ScheduleDto;

class ScheduleControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllSchedules_returnsSeededSchedules() throws Exception {
        mockMvc.perform(get("/schedules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    void getSchedule_existingId_returnsSchedule() throws Exception {
        mockMvc.perform(get("/schedules/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value("2026-07-28"));
    }

    @Test
    void getSchedule_missingId_returns404() throws Exception {
        mockMvc.perform(get("/schedules/9999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createSchedule_persistsAndReturnsCreated() throws Exception {
        ScheduleDto request = new ScheduleDto();
        request.setDate(LocalDate.of(2026, 8, 1));

        mockMvc.perform(post("/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.date").value("2026-08-01"));
    }

    @Test
    void updateSchedule_existingId_updatesAndReturnsOk() throws Exception {
        ScheduleDto request = new ScheduleDto();
        request.setDate(LocalDate.of(2026, 8, 2));

        mockMvc.perform(put("/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value("2026-08-02"));
    }

    @Test
    void updateSchedule_missingId_returns404() throws Exception {
        ScheduleDto request = new ScheduleDto();
        request.setDate(LocalDate.of(2026, 8, 2));

        mockMvc.perform(put("/schedules/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    // Exercises DuplicateDataException via the schedules.date UNIQUE constraint.
    @Test
    void updateSchedule_duplicateDate_returns409WithMessage() throws Exception {
        ScheduleDto request = new ScheduleDto();
        request.setDate(LocalDate.of(2026, 7, 29));

        mockMvc.perform(put("/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(content().string("Duplicate value for date"));
    }

    @Test
    void createSchedule_duplicateDate_returns409WithMessage() throws Exception {
        ScheduleDto request = new ScheduleDto();
        request.setDate(LocalDate.of(2026, 7, 28));

        mockMvc.perform(post("/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(content().string("Duplicate value for date"));
    }

    // Exercises NullAssignmentException via the schedules.date NOT NULL constraint -
    // there's no application-level guard for this field.
    @Test
    void createSchedule_withNullDate_returns400WithMessage() throws Exception {
        ScheduleDto request = new ScheduleDto();
        request.setDate(null);

        mockMvc.perform(post("/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("date cannot be null"));
    }

    @Test
    void patchSchedule_updatesDateOnly_returnsOk() throws Exception {
        ScheduleDto request = new ScheduleDto();
        request.setDate(LocalDate.of(2026, 8, 3));

        mockMvc.perform(patch("/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value("2026-08-03"));
    }

    @Test
    void patchSchedule_missingId_returns404() throws Exception {
        ScheduleDto request = new ScheduleDto();
        request.setDate(LocalDate.of(2026, 8, 3));

        mockMvc.perform(patch("/schedules/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    // Exercises DuplicateDataException via PATCH's saveAndFlush(), same as PUT.
    @Test
    void patchSchedule_duplicateDate_returns409WithMessage() throws Exception {
        ScheduleDto request = new ScheduleDto();
        request.setDate(LocalDate.of(2026, 7, 29));

        mockMvc.perform(patch("/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(content().string("Duplicate value for date"));
    }

    // Regression test: PATCH is a partial update - an empty body must leave the
    // existing date untouched, unlike PUT which would reject/null it out.
    @Test
    void patchSchedule_withEmptyBody_leavesDateUnchanged() throws Exception {
        ScheduleDto request = new ScheduleDto();

        mockMvc.perform(patch("/schedules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value("2026-07-28"));
    }

    @Test
    void deleteSchedule_existingId_removesScheduleAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/schedules/3"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/schedules/3"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteSchedule_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/schedules/9999"))
            .andExpect(status().isNotFound());
    }

    // Regression test: unlike Category/Goal, deleting a Schedule cascades REMOVE to
    // dependent Events (the FK is NOT NULL, so there's no valid "unlinked" state) -
    // see context.md domain model notes. Schedule 1 owns events 1 and 2 in data.sql.
    @Test
    void deleteSchedule_cascadesDeleteToEvents() throws Exception {
        mockMvc.perform(delete("/schedules/1"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/events/1"))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/events/2"))
            .andExpect(status().isNotFound());
    }
}
