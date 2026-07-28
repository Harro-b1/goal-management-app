package com.harro.goaltracker.controllers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
