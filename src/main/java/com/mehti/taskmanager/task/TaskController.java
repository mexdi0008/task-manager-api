package com.mehti.taskmanager.task;

import com.mehti.taskmanager.task.dto.PageResponse;
import com.mehti.taskmanager.task.dto.TaskRequest;
import com.mehti.taskmanager.task.dto.TaskResponse;
import com.mehti.taskmanager.user.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tasks")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@GetMapping
	public PageResponse<TaskResponse> list(@AuthenticationPrincipal User user,
										   @RequestParam(required = false) TaskStatus status,
										   @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return taskService.list(user, status, pageable);
	}

	@GetMapping("/{id}")
	public TaskResponse get(@AuthenticationPrincipal User user, @PathVariable Long id) {
		return taskService.get(user, id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TaskResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody TaskRequest request) {
		return taskService.create(user, request);
	}

	@PutMapping("/{id}")
	public TaskResponse update(@AuthenticationPrincipal User user, @PathVariable Long id, @Valid @RequestBody TaskRequest request) {
		return taskService.update(user, id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
		taskService.delete(user, id);
	}
}
