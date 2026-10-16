package com.taskflow.taskflow.dto;

// A record is an immutable data class. Jackson turns it into JSON automatically.
public record TaskSummary(
        long total,
        long pending,
        long inProgress,
        long completed
) {
}