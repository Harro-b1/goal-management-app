package com.harro.goaltracker.controllers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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
import com.harro.goaltracker.dtos.CategoryDto;

class CategoryControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCategories_returnsSeededCategories() throws Exception {
        mockMvc.perform(get("/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void getCategory_existingId_returnsCategory() throws Exception {
        mockMvc.perform(get("/categories/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Health"));
    }

    @Test
    void getCategory_missingId_returns404() throws Exception {
        mockMvc.perform(get("/categories/9999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createCategory_persistsAndReturnsCreated() throws Exception {
        CategoryDto request = new CategoryDto();
        request.setName("Finance");

        mockMvc.perform(post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.name").value("Finance"));
    }

    @Test
    void updateCategory_existingId_updatesAndReturnsOk() throws Exception {
        CategoryDto request = new CategoryDto();
        request.setName("Wellness");

        mockMvc.perform(put("/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Wellness"));
    }

    @Test
    void updateCategory_missingId_returns404() throws Exception {
        CategoryDto request = new CategoryDto();
        request.setName("Doesn't matter");

        mockMvc.perform(put("/categories/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    // Exercises DuplicateDataException via the categories.name UNIQUE constraint.
    @Test
    void updateCategory_duplicateName_returns409WithMessage() throws Exception {
        CategoryDto request = new CategoryDto();
        request.setName("Career");

        mockMvc.perform(put("/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(content().string("Duplicate value for name"));
    }

    @Test
    void createCategory_duplicateName_returns409WithMessage() throws Exception {
        CategoryDto request = new CategoryDto();
        request.setName("Health");

        mockMvc.perform(post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(content().string("Duplicate value for name"));
    }

    // Exercises NullAssignmentException via the categories.name NOT NULL constraint -
    // there's no application-level guard for this field.
    @Test
    void createCategory_withNullName_returns400WithMessage() throws Exception {
        CategoryDto request = new CategoryDto();
        request.setName(null);

        mockMvc.perform(post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("name cannot be null"));
    }

    @Test
    void deleteCategory_existingId_removesCategoryAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/categories/2"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/categories/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/categories/9999"))
            .andExpect(status().isNotFound());
    }

    // Regression test: deleting a Category strips the reference from dependent Goals
    // instead of cascading delete - see context.md domain model notes. Category 1
    // ("Health") is referenced by goals 1, 2 and 5 in data.sql.
    @Test
    void deleteCategory_stripsCategoryFromGoals() throws Exception {
        mockMvc.perform(delete("/categories/1"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/goals/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.category").value(org.hamcrest.Matchers.nullValue()));
    }
}
