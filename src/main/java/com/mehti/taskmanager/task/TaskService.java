package com.mehti.taskmanager.task;

import com.mehti.taskmanager.common.NotFoundException;
import com.mehti.taskmanager.task.dto.PageResponse;
import com.mehti.taskmanager.task.dto.TaskRequest;
import com.mehti.taskmanager.task.dto.TaskResponse;
import com.mehti.taskmanager.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

	private final TaskRepository tasks;

	public TaskService(TaskRepository tasks) {
		this.tasks = tasks;
	}

	@Transactional(readOnly = true)
	public PageResponse<TaskResponse> list(User owner, TaskStatus status, Pageable pageable) {
		Page<Task> page = status == null
				? tasks.findByOwnerId(owner.getId(), pageable)
				: tasks.findByOwnerIdAndStatus(owner.getId(), status, pageable);
		return PageResponse.of(page, TaskResponse::from);
	}

	@Transactional(readOnly = true)
	public TaskResponse get(User owner, Long id) {
		return TaskResponse.from(find(owner, id));
	}

	@Transactional
	public TaskResponse create(User owner, TaskRequest request) {
		Task task = new Task(owner);
		apply(task, request);
		return TaskResponse.from(tasks.save(task));
	}

	@Transactional
	public TaskResponse update(User owner, Long id, TaskRequest request) {
		Task task = find(owner, id);
		apply(task, request);
		return TaskResponse.from(tasks.saveAndFlush(task));
	}

	@Transactional
	public void delete(User owner, Long id) {
		tasks.delete(find(owner, id));
	}

	private Task find(User owner, Long id) {
		return tasks.findByIdAndOwnerId(id, owner.getId())
				.orElseThrow(() -> new NotFoundException("Task " + id + " not found"));
	}

	private static void apply(Task task, TaskRequest request) {
		task.setTitle(request.title().trim());
		task.setDescription(request.description());
		task.setDueDate(request.dueDate());
		if (request.status() != null) {
			task.setStatus(request.status());
		}
		if (request.priority() != null) {
			task.setPriority(request.priority());
		}
	}
}
