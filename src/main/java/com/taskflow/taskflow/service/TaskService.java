package com.taskflow.taskflow.service;

import com.taskflow.taskflow.dto.TaskSummary;
import com.taskflow.taskflow.entity.Priority;
import com.taskflow.taskflow.entity.Status;
import com.taskflow.taskflow.entity.Task;
import com.taskflow.taskflow.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.taskflow.taskflow.exception.TaskNotFoundException;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(Task task) {
        task.setId(null);
        return taskRepository.save(task);
    }

    // Returns all tasks when both filters are null
    public List<Task> getTasks(Status status, Priority priority) {
        return taskRepository.findWithFilters(status, priority);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task updateTask(Long id, Task updated) {
        Task existing = getTaskById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setPriority(updated.getPriority());
        existing.setStatus(updated.getStatus());
        existing.setDueDate(updated.getDueDate());
        return taskRepository.save(existing);
    }

    public void deleteTask(Long id) {
        Task existing = getTaskById(id);
        taskRepository.delete(existing);
    }

    public TaskSummary getSummary() {
        return new TaskSummary(
                taskRepository.count(),
                taskRepository.countByStatus(Status.PENDING),
                taskRepository.countByStatus(Status.IN_PROGRESS),
                taskRepository.countByStatus(Status.COMPLETED)
        );
    }
}