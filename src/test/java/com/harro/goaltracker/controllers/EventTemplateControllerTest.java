package com.harro.goaltracker.controllers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
            .andExpect(status().isBadRequest())
            .andExpect(content().string("scheduleTemplate cannot be null"));
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

    // Regression test: updateEventTemplate used to mutate the managed entity via the
    // mapper before validating scheduleTemplate/goal, so an invalid scheduleTemplate
    // still leaked the name change despite the request being rejected with 422.
    @Test
    void updateEventTemplate_withInvalidScheduleTemplate_doesNotPersistPartialUpdate() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setScheduleTemplate(9999L);
        request.setName("Mutated Name Should Not Persist");
        request.setStartTime(LocalTime.of(23, 0));
        request.setEndTime(LocalTime.of(23, 30));

        mockMvc.perform(put("/event-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422));

        mockMvc.perform(get("/event-templates/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Morning run"));
    }

    @Test
    void updateEventTemplate_withInvalidGoal_returns422() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setScheduleTemplate(1L);
        request.setGoal(9999L);
        request.setName("Mutated Name Should Not Persist");
        request.setStartTime(LocalTime.of(23, 0));
        request.setEndTime(LocalTime.of(23, 30));

        mockMvc.perform(put("/event-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422))
            .andExpect(content().string("Invalid goal reference id"));

        mockMvc.perform(get("/event-templates/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Morning run"));
    }

    @Test
    void updateEventTemplate_existingId_updatesAndReturnsOk() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setScheduleTemplate(2L);
        request.setGoal(3L);
        request.setName("Updated template name");
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(8, 45));

        mockMvc.perform(put("/event-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated template name"))
            .andExpect(jsonPath("$.scheduleTemplate").value(2))
            .andExpect(jsonPath("$.goal").value(3));
    }

    @Test
    void updateEventTemplate_missingId_returns404() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setScheduleTemplate(1L);
        request.setName("Doesn't matter");
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(8, 30));

        mockMvc.perform(put("/event-templates/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateEventTemplate_withoutScheduleTemplate_returns400WithMessage() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setName("Doesn't matter");
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(8, 30));

        mockMvc.perform(put("/event-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("scheduleTemplate cannot be null"));
    }

    // Regression test: PUT sending goal:null used to be silently ignored (treated as
    // "leave unchanged") instead of clearing the relation - see context.md bug #5.
    @Test
    void updateEventTemplate_withNullGoal_clearsExistingGoal() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setScheduleTemplate(1L);
        request.setGoal(null);
        request.setName("Morning run");
        request.setStartTime(LocalTime.of(7, 0));
        request.setEndTime(LocalTime.of(7, 30));

        mockMvc.perform(put("/event-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.goal").value(nullValue()));
    }

    @Test
    void patchEventTemplate_updatesNameOnly_leavesRelationsUnchanged() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setName("Renamed via patch");

        mockMvc.perform(patch("/event-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Renamed via patch"))
            .andExpect(jsonPath("$.scheduleTemplate").value(1))
            .andExpect(jsonPath("$.goal").value(1));
    }

    @Test
    void patchEventTemplate_missingId_returns404() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setName("Doesn't matter");

        mockMvc.perform(patch("/event-templates/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void patchEventTemplate_updatesScheduleTemplate_returnsOk() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setScheduleTemplate(2L);

        mockMvc.perform(patch("/event-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scheduleTemplate").value(2));
    }

    @Test
    void patchEventTemplate_withInvalidScheduleTemplate_returns422() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setScheduleTemplate(9999L);

        mockMvc.perform(patch("/event-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422))
            .andExpect(content().string("Invalid scheduleTemplate reference id"));
    }

    @Test
    void patchEventTemplate_updatesGoal_returnsOk() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setGoal(3L);

        mockMvc.perform(patch("/event-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.goal").value(3));
    }

    @Test
    void patchEventTemplate_withInvalidGoal_returns422() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setGoal(9999L);

        mockMvc.perform(patch("/event-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422))
            .andExpect(content().string("Invalid goal reference id"));
    }

    // PATCH's partial-update contract: omitting "goal" must leave the existing
    // relation untouched, unlike PUT where an omitted/null goal clears it.
    @Test
    void patchEventTemplate_omittingGoal_leavesGoalUnchanged() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setName("Renamed, goal untouched");

        mockMvc.perform(patch("/event-templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.goal").value(1));
    }

    @Test
    void deleteEventTemplate_existingId_removesEventTemplateAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/event-templates/1"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/event-templates/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteEventTemplate_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/event-templates/9999"))
            .andExpect(status().isNotFound());
    }

    // Exercises NullAssignmentException: event_templates.name has no application-level
    // guard, so a null name reaches the DB's NOT NULL constraint.
    @Test
    void createEventTemplate_withNullName_returns400WithMessage() throws Exception {
        EventTemplateDto request = new EventTemplateDto();
        request.setScheduleTemplate(1L);
        request.setName(null);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 30));

        mockMvc.perform(post("/event-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("name cannot be null"));
    }
}
