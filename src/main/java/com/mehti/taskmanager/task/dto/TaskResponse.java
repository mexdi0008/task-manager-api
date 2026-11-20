package com.mehti.taskmanager.task.dto;

import com.mehti.taskmanager.task.Task;
import com.mehti.taskmanager.task.TaskPriority;
import com.mehti.taskmanager.task.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(Long id, String title, String description, TaskStatus status, TaskPriority priority,
						   LocalDate dueDate, Instant createdAt, Instant updatedAt) {

	public static TaskResponse from(Task task) {
		return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(),
				task.getPriority(), task.getDueDate(), task.getCreatedAt(), task.getUpdatedAt());
	}
}
