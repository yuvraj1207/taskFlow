package com.taskflow.taskflow.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Integration test: starts the full app and calls the real endpoints.
// @Transactional rolls back every test, so your dev data is not changed.
@SpringBootTest
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private static final String VALID_TASK = """
            {
              "title": "Integration test task",
              "description": "Created by a test",
              "priority": "HIGH",
              "status": "PENDING",
              "dueDate": "2026-10-20"
            }
            """;

    @Test
    void createTask_returns201WithId() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TASK))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Integration test task"));
    }

    @Test
    void createTask_blankTitle_returns400WithValidationMessage() throws Exception {
        String body = """
                {"title": "", "priority": "LOW", "status": "PENDING"}
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("title: must not be blank"));
    }

    @Test
    void getTaskById_missing_returns404WithMessage() throws Exception {
        mockMvc.perform(get("/api/tasks/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id 999999"));
    }

    @Test
    void getSummary_isNotTreatedAsId() throws Exception {
        mockMvc.perform(get("/api/tasks/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").exists());
    }

    @Test
    void getTasks_filterByInvalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", "DONE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'DONE' for parameter 'status'"));
    }

    @Test
    void createThenFilterByStatus_findsTheTask() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TASK))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title").value(org.hamcrest.Matchers.hasItem("Integration test task")));
    }
}