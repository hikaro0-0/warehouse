package com.hikaro.warehouse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hikaro.warehouse.dto.AsyncTaskStatus;
import com.hikaro.warehouse.dto.AsyncTaskStatusResponseDto;
import com.hikaro.warehouse.dto.AsyncTaskSubmissionResponseDto;
import com.hikaro.warehouse.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class AsyncTaskServiceTest {

    private final AsyncTaskService asyncTaskService = new AsyncTaskService();

    @Test
    void shouldTrackAsyncTaskLifecycle() {
        AsyncTaskSubmissionResponseDto submission = asyncTaskService.createTask();

        assertNotNull(submission.taskId());
        assertEquals(AsyncTaskStatus.PENDING, submission.status());

        AsyncTaskStatusResponseDto pendingStatus =
                asyncTaskService.getTaskStatus(submission.taskId());
        assertEquals(AsyncTaskStatus.PENDING, pendingStatus.status());
        assertNotNull(pendingStatus.createdAt());
        assertNull(pendingStatus.startedAt());
        assertNull(pendingStatus.completedAt());
        assertNull(pendingStatus.errorMessage());

        asyncTaskService.markRunning(submission.taskId());
        AsyncTaskStatusResponseDto runningStatus =
                asyncTaskService.getTaskStatus(submission.taskId());
        assertEquals(AsyncTaskStatus.RUNNING, runningStatus.status());
        assertNotNull(runningStatus.startedAt());
        assertNull(runningStatus.completedAt());

        asyncTaskService.markCompleted(submission.taskId());
        AsyncTaskStatusResponseDto completedStatus =
                asyncTaskService.getTaskStatus(submission.taskId());
        assertEquals(AsyncTaskStatus.COMPLETED, completedStatus.status());
        assertNotNull(completedStatus.startedAt());
        assertNotNull(completedStatus.completedAt());
        assertNull(completedStatus.errorMessage());
    }

    @Test
    void shouldStoreFailureReason() {
        AsyncTaskSubmissionResponseDto submission = asyncTaskService.createTask();

        asyncTaskService.markFailed(
                submission.taskId(),
                new IllegalStateException("Async processing failed")
        );

        AsyncTaskStatusResponseDto failedStatus =
                asyncTaskService.getTaskStatus(submission.taskId());
        assertEquals(AsyncTaskStatus.FAILED, failedStatus.status());
        assertNotNull(failedStatus.startedAt());
        assertNotNull(failedStatus.completedAt());
        assertEquals("Async processing failed", failedStatus.errorMessage());
    }

    @Test
    void shouldThrowWhenTaskIsMissing() {
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> asyncTaskService.getTaskStatus("missing-task")
        );

        assertEquals("Async task with id 'missing-task' not found", exception.getMessage());
    }
}
