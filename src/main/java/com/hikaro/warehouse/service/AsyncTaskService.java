package com.hikaro.warehouse.service;

import com.hikaro.warehouse.dto.AsyncTaskStatus;
import com.hikaro.warehouse.dto.AsyncTaskStatusResponseDto;
import com.hikaro.warehouse.dto.AsyncTaskSubmissionResponseDto;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskService {

    private final Map<String, AsyncTaskState> tasks = new ConcurrentHashMap<>();

    public AsyncTaskSubmissionResponseDto createTask() {
        String taskId = UUID.randomUUID().toString();
        AsyncTaskState state = AsyncTaskState.pending(taskId);
        tasks.put(taskId, state);
        return state.toSubmissionResponse();
    }

    public AsyncTaskStatusResponseDto getTaskStatus(String taskId) {
        return getExistingTask(taskId).toStatusResponse();
    }

    public void markRunning(String taskId) {
        updateTask(taskId, AsyncTaskState::markRunning);
    }

    public void markCompleted(String taskId) {
        updateTask(taskId, AsyncTaskState::markCompleted);
    }

    public void markFailed(String taskId, Throwable throwable) {
        updateTask(taskId, task -> task.markFailed(resolveErrorMessage(throwable)));
    }

    private AsyncTaskState getExistingTask(String taskId) {
        AsyncTaskState task = tasks.get(taskId);
        if (task == null) {
            throw new ResourceNotFoundException(
                    "Async task with id '" + taskId + "' not found"
            );
        }
        return task;
    }

    private void updateTask(String taskId, UnaryOperator<AsyncTaskState> updater) {
        AsyncTaskState updatedTask = tasks.computeIfPresent(
                taskId,
                (ignored, currentTask) -> updater.apply(currentTask)
        );
        if (updatedTask == null) {
            throw new ResourceNotFoundException(
                    "Async task with id '" + taskId + "' not found"
            );
        }
    }

    private String resolveErrorMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return message;
    }

    private record AsyncTaskState(
            String taskId,
            AsyncTaskStatus status,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt,
            String errorMessage
    ) {

        private static AsyncTaskState pending(String taskId) {
            return new AsyncTaskState(
                    taskId,
                    AsyncTaskStatus.PENDING,
                    Instant.now(),
                    null,
                    null,
                    null
            );
        }

        private AsyncTaskState markRunning() {
            Instant currentStartedAt = startedAt != null ? startedAt : Instant.now();
            return new AsyncTaskState(
                    taskId,
                    AsyncTaskStatus.RUNNING,
                    createdAt,
                    currentStartedAt,
                    null,
                    null
            );
        }

        private AsyncTaskState markCompleted() {
            Instant currentStartedAt = startedAt != null ? startedAt : Instant.now();
            return new AsyncTaskState(
                    taskId,
                    AsyncTaskStatus.COMPLETED,
                    createdAt,
                    currentStartedAt,
                    Instant.now(),
                    null
            );
        }

        private AsyncTaskState markFailed(String currentErrorMessage) {
            Instant currentStartedAt = startedAt != null ? startedAt : Instant.now();
            return new AsyncTaskState(
                    taskId,
                    AsyncTaskStatus.FAILED,
                    createdAt,
                    currentStartedAt,
                    Instant.now(),
                    currentErrorMessage
            );
        }

        private AsyncTaskSubmissionResponseDto toSubmissionResponse() {
            return new AsyncTaskSubmissionResponseDto(taskId, status);
        }

        private AsyncTaskStatusResponseDto toStatusResponse() {
            return new AsyncTaskStatusResponseDto(
                    taskId,
                    status,
                    createdAt,
                    startedAt,
                    completedAt,
                    errorMessage
            );
        }
    }
}
