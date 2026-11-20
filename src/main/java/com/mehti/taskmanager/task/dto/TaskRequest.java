package com.mehti.taskmanager.task.dto;

import com.mehti.taskmanager.task.TaskPriority;
import com.mehti.taskmanager.task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
		@NotBlank @Size(max = 150) String title,
		@Size(max = 2000) String description,
		TaskStatus status,
		TaskPriority priority,
		LocalDate dueDate
) {
}
