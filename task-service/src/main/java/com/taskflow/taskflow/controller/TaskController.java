package com.taskflow.taskflow.controller;

import com.taskflow.taskflow.dto.TaskSummary;
import com.taskflow.taskflow.entity.Priority;
import com.taskflow.taskflow.entity.Status;
import com.taskflow.taskflow.entity.Task;
import com.taskflow.taskflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // POST /api/tasks
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        Task saved = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // GET /api/tasks
    // GET /api/tasks?status=COMPLETED
    // GET /api/tasks?priority=HIGH
    // GET /api/tasks?status=PENDING&priority=HIGH
    @GetMapping
    public List<Task> getTasks(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority) {
        return taskService.getTasks(status, priority);
    }

    // GET /api/tasks/summary
    @GetMapping("/summary")
    public TaskSummary getSummary() {
        return taskService.getSummary();
    }

    // GET /api/tasks/5
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    // PUT /api/tasks/5
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @Valid @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    // DELETE /api/tasks/5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}