package com.harro.goaltracker.controllers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import com.harro.goaltracker.dtos.GoalDto;
import com.harro.goaltracker.enums.PriorityLevel;

class GoalControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllGoals_returnsSeededGoals() throws Exception {
        mockMvc.perform(get("/goals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))));
    }

    @Test
    void getGoal_existingId_returnsGoal() throws Exception {
        mockMvc.perform(get("/goals/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Run a 5k"));
    }

    @Test
    void getGoal_missingId_returns404() throws Exception {
        mockMvc.perform(get("/goals/9999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void searchGoals_matchesRegexAgainstNameOrDescription() throws Exception {
        mockMvc.perform(get("/goals/search").param("query", "^Run"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Run a 5k"));
    }

    @Test
    void createGoal_withCategory_persistsAndReturnsCreated() throws Exception {
        GoalDto request = new GoalDto();
        request.setName("Test Goal");
        request.setDescription("Test Description");
        request.setCategory(1L);
        request.setPriority(PriorityLevel.MEDIUM);

        mockMvc.perform(post("/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.name").value("Test Goal"))
            .andExpect(jsonPath("$.category").value(1));
    }

    @Test
    void createGoal_withoutCategory_persistsWithNullCategory() throws Exception {
        GoalDto request = new GoalDto();
        request.setName("No Category Goal");
        request.setPriority(PriorityLevel.LOW);

        mockMvc.perform(post("/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.category").value(nullValue()));
    }

    @Test
    void createGoal_withFinishByDate_persistsAndReturnsIt() throws Exception {
        GoalDto request = new GoalDto();
        request.setName("Goal With Deadline");
        request.setPriority(PriorityLevel.MEDIUM);
        request.setFinishByDate(LocalDate.of(2026, 12, 31));

        mockMvc.perform(post("/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.finishByDate").value("2026-12-31"));
    }

    @Test
    void createGoal_withoutFinishByDate_persistsWithNullFinishByDate() throws Exception {
        GoalDto request = new GoalDto();
        request.setName("No Deadline Goal");
        request.setPriority(PriorityLevel.LOW);

        mockMvc.perform(post("/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.finishByDate").value(nullValue()));
    }

    @Test
    void createGoal_withInvalidCategory_returns422() throws Exception {
        GoalDto request = new GoalDto();
        request.setName("Bad Category Goal");
        request.setCategory(9999L);
        request.setPriority(PriorityLevel.HIGH);

        mockMvc.perform(post("/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422));
    }

    // Regression test: updateGoal used to mutate the managed entity via the mapper
    // before validating the category, so an invalid category still leaked the name
    // change to the database despite the request being rejected with 422.
    @Test
    void updateGoal_withInvalidCategory_doesNotPersistPartialUpdate() throws Exception {
        GoalDto request = new GoalDto();
        request.setName("Mutated Name Should Not Persist");
        request.setCategory(9999L);
        request.setPriority(PriorityLevel.HIGH);

        mockMvc.perform(put("/goals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422));

        mockMvc.perform(get("/goals/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Run a 5k"));
    }

    @Test
    void updateGoal_existingId_updatesAndReturnsOk() throws Exception {
        GoalDto request = new GoalDto();
        request.setName("Updated Goal Name");
        request.setDescription("Updated description");
        request.setCategory(2L);
        request.setPriority(PriorityLevel.HIGH);

        mockMvc.perform(put("/goals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Goal Name"))
            .andExpect(jsonPath("$.category").value(2));
    }

    @Test
    void updateGoal_missingId_returns404() throws Exception {
        GoalDto request = new GoalDto();
        request.setName("Doesn't matter");
        request.setPriority(PriorityLevel.LOW);

        mockMvc.perform(put("/goals/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    // Regression test: PUT sending category:null used to be silently ignored (treated
    // as "leave unchanged") instead of clearing the relation - see context.md bug #5.
    @Test
    void updateGoal_withNullCategory_clearsExistingCategory() throws Exception {
        GoalDto request = new GoalDto();
        request.setName("Run a 5k");
        request.setCategory(null);
        request.setPriority(PriorityLevel.MEDIUM);

        mockMvc.perform(put("/goals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.category").value(nullValue()));
    }

    // PUT is a full replacement: an omitted/null finishByDate must clear an existing one,
    // same contract already exercised for category (see updateGoal_withNullCategory_clearsExistingCategory).
    @Test
    void updateGoal_withNullFinishByDate_clearsExistingFinishByDate() throws Exception {
        GoalDto createRequest = new GoalDto();
        createRequest.setName("Goal With Deadline");
        createRequest.setPriority(PriorityLevel.MEDIUM);
        createRequest.setFinishByDate(LocalDate.of(2026, 12, 31));

        String createResponse = mockMvc.perform(post("/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readValue(createResponse, GoalDto.class).getId();

        GoalDto updateRequest = new GoalDto();
        updateRequest.setName("Goal With Deadline");
        updateRequest.setPriority(PriorityLevel.MEDIUM);

        mockMvc.perform(put("/goals/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.finishByDate").value(nullValue()));
    }

    @Test
    void patchGoal_updatesNameOnly_returnsOk() throws Exception {
        GoalDto request = new GoalDto();
        request.setName("Renamed via patch");

        mockMvc.perform(patch("/goals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Renamed via patch"));
    }

    @Test
    void patchGoal_missingId_returns404() throws Exception {
        GoalDto request = new GoalDto();
        request.setName("Doesn't matter");

        mockMvc.perform(patch("/goals/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void patchGoal_updatesCategory_returnsOk() throws Exception {
        GoalDto request = new GoalDto();
        request.setCategory(2L);

        mockMvc.perform(patch("/goals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.category").value(2));
    }

    @Test
    void patchGoal_withInvalidCategory_returns422() throws Exception {
        GoalDto request = new GoalDto();
        request.setCategory(9999L);

        mockMvc.perform(patch("/goals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is(422))
            .andExpect(content().string("Invalid category reference id"));
    }

    // PATCH's partial-update contract: omitting "category" must leave the existing
    // relation untouched, unlike PUT where an omitted/null category clears it.
    @Test
    void patchGoal_omittingCategory_leavesCategoryUnchanged() throws Exception {
        GoalDto request = new GoalDto();
        request.setName("Renamed, category untouched");

        mockMvc.perform(patch("/goals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.category").value(1));
    }

    // PATCH's partial-update contract: omitting "finishByDate" must leave the existing
    // value untouched, unlike PUT where an omitted/null finishByDate clears it.
    @Test
    void patchGoal_omittingFinishByDate_leavesFinishByDateUnchanged() throws Exception {
        GoalDto createRequest = new GoalDto();
        createRequest.setName("Goal With Deadline");
        createRequest.setPriority(PriorityLevel.MEDIUM);
        createRequest.setFinishByDate(LocalDate.of(2026, 12, 31));

        String createResponse = mockMvc.perform(post("/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readValue(createResponse, GoalDto.class).getId();

        GoalDto patchRequest = new GoalDto();
        patchRequest.setName("Renamed, deadline untouched");

        mockMvc.perform(patch("/goals/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.finishByDate").value("2026-12-31"));
    }

    // Regression test: GoalDto.completed is a primitive boolean, so an omitted field
    // deserializes to false (not null) and NullValuePropertyMappingStrategy.IGNORE
    // can't skip it - patchGoal must explicitly ignore "completed" in the mapper or
    // every PATCH silently un-completes the goal. See GoalMapper#patchGoal.
    @Test
    void patchGoal_afterComplete_doesNotResetCompleted() throws Exception {
        mockMvc.perform(put("/goals/2/complete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(true));

        GoalDto request = new GoalDto();
        request.setName("Renamed after completing");

        mockMvc.perform(patch("/goals/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Renamed after completing"))
            .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void deleteGoal_existingId_removesGoalAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/goals/2"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/goals/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteGoal_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/goals/9999"))
            .andExpect(status().isNotFound());
    }

    // Regression test: deleting a Goal strips the reference from dependent Events and
    // EventTemplates instead of cascading delete - see context.md domain model notes.
    // Goal 1 is referenced by event 1 and event template 1 in data.sql.
    @Test
    void deleteGoal_stripsGoalFromEventsAndEventTemplates() throws Exception {
        mockMvc.perform(delete("/goals/1"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/events/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.goal").value(nullValue()));

        mockMvc.perform(get("/event-templates/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.goal").value(nullValue()));
    }

    // Exercises NullAssignmentException: goals.name has no application-level guard,
    // so a null name reaches the DB's NOT NULL constraint.
    @Test
    void createGoal_withNullName_returns400WithMessage() throws Exception {
        GoalDto request = new GoalDto();
        request.setName(null);
        request.setPriority(PriorityLevel.MEDIUM);

        mockMvc.perform(post("/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("name cannot be null"));
    }

    @Test
    void completeGoal_marksCompletedTrue() throws Exception {
        mockMvc.perform(put("/goals/2/complete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void completeGoal_missingId_returns404() throws Exception {
        mockMvc.perform(put("/goals/9999/complete"))
            .andExpect(status().isNotFound());
    }

    @Test
    void uncompleteGoal_marksCompletedFalse() throws Exception {
        mockMvc.perform(put("/goals/1/uncomplete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void uncompleteGoal_missingId_returns404() throws Exception {
        mockMvc.perform(put("/goals/9999/uncomplete"))
            .andExpect(status().isNotFound());
    }
}
