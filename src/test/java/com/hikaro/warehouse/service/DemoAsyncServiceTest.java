package com.hikaro.warehouse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hikaro.warehouse.dto.AsyncTaskStatus;
import com.hikaro.warehouse.dto.AsyncTaskStatusResponseDto;
import com.hikaro.warehouse.dto.AsyncTaskSubmissionResponseDto;
import com.hikaro.warehouse.dto.BulkOperationRequestDto;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemoAsyncServiceTest {

    @Mock
    private AsyncTaskService asyncTaskService;

    @Mock
    private DemoAsyncTaskProcessor demoAsyncTaskProcessor;

    @InjectMocks
    private DemoAsyncService demoAsyncService;

    @Test
    void shouldCreateTaskAndTriggerAsyncProcessing() {
        BulkOperationRequestDto request = buildRequest();
        AsyncTaskSubmissionResponseDto submission =
                new AsyncTaskSubmissionResponseDto("task-1", AsyncTaskStatus.PENDING);

        when(asyncTaskService.createTask()).thenReturn(submission);

        AsyncTaskSubmissionResponseDto response =
                demoAsyncService.startGraphSaveWithTransaction(request);

        assertEquals(submission, response);
        verify(asyncTaskService).createTask();
        verify(demoAsyncTaskProcessor).processGraphSave("task-1", request);
    }

    @Test
    void shouldReturnStoredTaskStatus() {
        AsyncTaskStatusResponseDto status = new AsyncTaskStatusResponseDto(
                "task-1",
                AsyncTaskStatus.COMPLETED,
                Instant.parse("2026-04-21T08:00:00Z"),
                Instant.parse("2026-04-21T08:00:01Z"),
                Instant.parse("2026-04-21T08:00:02Z"),
                null
        );

        when(asyncTaskService.getTaskStatus("task-1")).thenReturn(status);

        AsyncTaskStatusResponseDto response = demoAsyncService.getTaskStatus("task-1");

        assertEquals(status, response);
        verify(asyncTaskService).getTaskStatus("task-1");
    }

    private BulkOperationRequestDto buildRequest() {
        return new BulkOperationRequestDto(
                "Supplier",
                "mail@example.com",
                "Warehouse",
                "Street 1",
                "Product",
                "SKU-1",
                3,
                List.of(1L, 2L)
        );
    }
}
