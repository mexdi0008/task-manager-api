package com.mehti.taskmanager.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

	Page<Task> findByOwnerId(Long ownerId, Pageable pageable);

	Page<Task> findByOwnerIdAndStatus(Long ownerId, TaskStatus status, Pageable pageable);

	/** Owner check is part of the query, so users can never read or modify other users' tasks. */
	Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);
}
