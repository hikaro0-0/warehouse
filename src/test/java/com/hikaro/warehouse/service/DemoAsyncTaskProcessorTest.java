package com.hikaro.warehouse.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.hikaro.warehouse.dto.BulkOperationRequestDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemoAsyncTaskProcessorTest {

    @Mock
    private AsyncTaskService asyncTaskService;

    @Mock
    private DemoService demoService;

    @InjectMocks
    private DemoAsyncTaskProcessor demoAsyncTaskProcessor;

    @Test
    void shouldMarkTaskCompletedWhenProcessingSucceeds() {
        BulkOperationRequestDto request = buildRequest();

        CompletableFuture<Void> future =
                demoAsyncTaskProcessor.processGraphSave("task-1", request);

        assertTrue(future.isDone());
        verify(asyncTaskService).markRunning("task-1");
        verify(demoService).saveGraphForAsyncTask(request);
        verify(asyncTaskService).markCompleted("task-1");
        verify(asyncTaskService, never()).markFailed(eq("task-1"), any());
    }

    @Test
    void shouldMarkTaskFailedWhenProcessingThrowsException() {
        BulkOperationRequestDto request = buildRequest();
        doThrow(new IllegalStateException("boom"))
                .when(demoService)
                .saveGraphForAsyncTask(request);

        CompletableFuture<Void> future =
                demoAsyncTaskProcessor.processGraphSave("task-1", request);

        assertTrue(future.isDone());
        verify(asyncTaskService).markRunning("task-1");
        verify(demoService).saveGraphForAsyncTask(request);
        verify(asyncTaskService, never()).markCompleted("task-1");
        verify(asyncTaskService).markFailed(eq("task-1"), any(IllegalStateException.class));
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
