package com.taskflow.taskflow.service;

import com.taskflow.taskflow.dto.TaskSummary;
import com.taskflow.taskflow.entity.Priority;
import com.taskflow.taskflow.entity.Status;
import com.taskflow.taskflow.entity.Task;
import com.taskflow.taskflow.exception.TaskNotFoundException;
import com.taskflow.taskflow.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Unit test: no Spring context, no database. The repository is a fake.
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_clearsIdAndSaves() {
        Task input = new Task("Learn JPA", "desc", Priority.HIGH, Status.PENDING, null);
        input.setId(99L); // client should not be able to choose the ID

        when(taskRepository.save(input)).thenReturn(input);

        Task result = taskService.createTask(input);

        assertNull(input.getId());
        assertSame(input, result);
        verify(taskRepository).save(input);
    }

    @Test
    void getTaskById_returnsTaskWhenFound() {
        Task task = new Task("Task", null, Priority.LOW, Status.PENDING, null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertSame(task, result);
    }

    @Test
    void getTaskById_throwsWhenMissing() {
        when(taskRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(404L));
    }

    @Test
    void deleteTask_throwsAndDoesNotDeleteWhenMissing() {
        when(taskRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(404L));
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void getSummary_countsEachStatus() {
        when(taskRepository.count()).thenReturn(4L);
        when(taskRepository.countByStatus(Status.PENDING)).thenReturn(2L);
        when(taskRepository.countByStatus(Status.IN_PROGRESS)).thenReturn(1L);
        when(taskRepository.countByStatus(Status.COMPLETED)).thenReturn(1L);

        TaskSummary summary = taskService.getSummary();

        assertEquals(4L, summary.total());
        assertEquals(2L, summary.pending());
        assertEquals(1L, summary.inProgress());
        assertEquals(1L, summary.completed());
    }
}