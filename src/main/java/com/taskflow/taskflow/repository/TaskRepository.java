package com.taskflow.taskflow.repository;

import com.taskflow.taskflow.entity.Priority;
import com.taskflow.taskflow.entity.Status;
import com.taskflow.taskflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Each filter is optional: passing null skips that condition
    @Query("""
            SELECT t FROM Task t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:priority IS NULL OR t.priority = :priority)
            """)
    List<Task> findWithFilters(@Param("status") Status status,
                               @Param("priority") Priority priority);

    // Derived query: Spring builds "SELECT COUNT(*) ... WHERE status = ?" from the method name
    long countByStatus(Status status);
}