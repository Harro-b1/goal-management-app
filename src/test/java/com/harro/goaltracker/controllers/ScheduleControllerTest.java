package com.harro.goaltracker.controllers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.langchain4j.model.chat.ChatModel;
import tools.jackson.databind.ObjectMapper;
import com.harro.goaltracker.AbstractIntegrationTest;
import com.harro.goaltracker.dtos.EventDto;
import com.harro.goaltracker.dtos.ScheduleDto;

class ScheduleControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // generateEvents calls out to a real Ollama model via OllamaChatService - mocked
    // here so these tests are deterministic and don't depend on a running local model.
    @MockitoBean
    private ChatModel chatModel;

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

    @Test
    void addEvents_existingSchedule_persistsAndReturnsCreatedEvents() throws Exception {
        var scheduleId = createSchedule(LocalDate.of(2026, 8, 10));

        EventDto first = new EventDto();
        first.setName("Morning workout");
        first.setStartTime(LocalTime.of(7, 0));
        first.setEndTime(LocalTime.of(7, 30));

        EventDto second = new EventDto();
        second.setGoal(1L);
        second.setName("Evening review");
        second.setStartTime(LocalTime.of(20, 0));
        second.setEndTime(LocalTime.of(20, 30));

        mockMvc.perform(put("/schedules/" + scheduleId + "/addEvents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(first, second))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].schedule").value(scheduleId))
            .andExpect(jsonPath("$[0].goal").value(nullValue()))
            .andExpect(jsonPath("$[1].schedule").value(scheduleId))
            .andExpect(jsonPath("$[1].goal").value(1));

        mockMvc.perform(get("/schedules/" + scheduleId + "/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void addEvents_missingScheduleId_returns404() throws Exception {
        EventDto request = new EventDto();
        request.setName("Orphan event");
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        mockMvc.perform(put("/schedules/9999/addEvents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(request))))
            .andExpect(status().isNotFound());
    }

    @Test
    void addEvents_withEmptyList_returnsEmptyListAndCreatesNothing() throws Exception {
        var scheduleId = createSchedule(LocalDate.of(2026, 8, 11));

        mockMvc.perform(put("/schedules/" + scheduleId + "/addEvents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    // Regression test: each EventDto's own `schedule` field must be ignored in favor
    // of the path id - addEvents is always scoped to the schedule in the URL.
    @Test
    void addEvents_eventBodySpecifiesDifferentSchedule_usesPathIdInstead() throws Exception {
        var scheduleId = createSchedule(LocalDate.of(2026, 8, 12));

        EventDto request = new EventDto();
        request.setSchedule(1L);
        request.setName("Redirected event");
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(10, 30));

        mockMvc.perform(put("/schedules/" + scheduleId + "/addEvents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(request))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].schedule").value(scheduleId));
    }

    // Regression test: addEvents used to save each event in its own transaction, so
    // a validation failure partway through the list (here: an invalid goal on the
    // second event) still left earlier events in the same request persisted. Now
    // ScheduleService#addEvents is @Transactional, so the whole batch rolls back
    // together on any failure - this pins the resulting error response. (The actual
    // rollback isn't independently observable from this test: AbstractIntegrationTest
    // wraps the whole test method in its own outer transaction, so addEvents' nested
    // @Transactional joins it rather than committing/rolling back on its own - see
    // the class-level @Transactional notes in context.md.)
    @Test
    void addEvents_withInvalidGoalMidList_returns422() throws Exception {
        var scheduleId = createSchedule(LocalDate.of(2026, 8, 13));

        EventDto valid = new EventDto();
        valid.setName("Would-be valid event");
        valid.setStartTime(LocalTime.of(8, 0));
        valid.setEndTime(LocalTime.of(8, 30));

        EventDto invalidGoal = new EventDto();
        invalidGoal.setGoal(9999L);
        invalidGoal.setName("Bad goal event");
        invalidGoal.setStartTime(LocalTime.of(9, 0));
        invalidGoal.setEndTime(LocalTime.of(9, 30));

        mockMvc.perform(put("/schedules/" + scheduleId + "/addEvents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(valid, invalidGoal))))
            .andExpect(status().is(422))
            .andExpect(content().string("Invalid goal reference id"));
    }

    // Exercises NullAssignmentException via the events.name NOT NULL constraint -
    // there's no application-level guard for this field, same as createEvent.
    @Test
    void addEvents_withNullName_returns400WithMessage() throws Exception {
        var scheduleId = createSchedule(LocalDate.of(2026, 8, 14));

        EventDto request = new EventDto();
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        mockMvc.perform(put("/schedules/" + scheduleId + "/addEvents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(request))))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("name cannot be null"));
    }

    private Long createSchedule(LocalDate date) throws Exception {
        ScheduleDto request = new ScheduleDto();
        request.setDate(date);

        var response = mockMvc.perform(post("/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readValue(response, ScheduleDto.class).getId();
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

    @Test
    void generateEvents_scheduleWithNoEvents_returnsSingleGeneratedEventCoveringWholeWindow() throws Exception {
        var scheduleId = createSchedule(LocalDate.of(2026, 8, 20));
        when(chatModel.chat(anyString())).thenReturn("Deep work block");

        mockMvc.perform(get("/schedules/" + scheduleId + "/generateEvents")
                .param("start", "09:00:00")
                .param("end", "17:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Deep work block"))
            .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
            .andExpect(jsonPath("$[0].endTime").value("17:00:00"))
            .andExpect(jsonPath("$[0].id").value(nullValue()))
            .andExpect(jsonPath("$[0].goal").value(nullValue()))
            .andExpect(jsonPath("$[0].schedule").value(nullValue()));

        verify(chatModel, times(1)).chat(anyString());
    }

    // Schedule 3 in data.sql has a single event, 20:00-21:00 ("Reading time") - the
    // free window around it should split into two generated events, not one.
    @Test
    void generateEvents_scheduleWithOneEventMidWindow_returnsGeneratedEventsForFreeGapsOnly() throws Exception {
        when(chatModel.chat(anyString())).thenReturn("Focused work");

        mockMvc.perform(get("/schedules/3/generateEvents")
                .param("start", "09:00:00")
                .param("end", "22:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
            .andExpect(jsonPath("$[0].endTime").value("20:00:00"))
            .andExpect(jsonPath("$[1].startTime").value("21:00:00"))
            .andExpect(jsonPath("$[1].endTime").value("22:00:00"));

        verify(chatModel, times(2)).chat(anyString());
    }

    @Test
    void generateEvents_missingScheduleId_returns404() throws Exception {
        mockMvc.perform(get("/schedules/9999/generateEvents"))
            .andExpect(status().isNotFound());
    }

    @Test
    void generateEvents_noQueryParams_defaultsToFullDayWindow() throws Exception {
        var scheduleId = createSchedule(LocalDate.of(2026, 8, 21));
        when(chatModel.chat(anyString())).thenReturn("Whatever the day brings");

        mockMvc.perform(get("/schedules/" + scheduleId + "/generateEvents"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].startTime").value("00:00:00"))
            .andExpect(jsonPath("$[0].endTime").value("23:59:59"));
    }

    // Confirms the free TimeSlot's actual start/end/duration reach the model prompt,
    // not just that some prompt was sent - see OllamaChatService/prompt.txt.
    @Test
    void generateEvents_passesFreeSlotDetailsToChatModelPrompt() throws Exception {
        var scheduleId = createSchedule(LocalDate.of(2026, 8, 22));
        when(chatModel.chat(anyString())).thenReturn("Generated activity");

        mockMvc.perform(get("/schedules/" + scheduleId + "/generateEvents")
                .param("start", "09:00:00")
                .param("end", "17:00:00"))
            .andExpect(status().isOk());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatModel).chat(promptCaptor.capture());
        String prompt = promptCaptor.getValue();

        assertTrue(prompt.contains("09:00"), "prompt did not contain the free slot's start time: " + prompt);
        assertTrue(prompt.contains("17:00"), "prompt did not contain the free slot's end time: " + prompt);
    }
}
