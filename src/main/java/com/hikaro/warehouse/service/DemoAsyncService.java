package com.hikaro.warehouse.service;

import com.hikaro.warehouse.dto.AsyncTaskStatusResponseDto;
import com.hikaro.warehouse.dto.AsyncTaskSubmissionResponseDto;
import com.hikaro.warehouse.dto.BulkOperationRequestDto;
import org.springframework.stereotype.Service;

@Service
public class DemoAsyncService {

    private final AsyncTaskService asyncTaskService;
    private final DemoAsyncTaskProcessor demoAsyncTaskProcessor;

    public DemoAsyncService(
            AsyncTaskService asyncTaskService,
            DemoAsyncTaskProcessor demoAsyncTaskProcessor
    ) {
        this.asyncTaskService = asyncTaskService;
        this.demoAsyncTaskProcessor = demoAsyncTaskProcessor;
    }

    public AsyncTaskSubmissionResponseDto startGraphSaveWithTransaction(
            BulkOperationRequestDto request
    ) {
        AsyncTaskSubmissionResponseDto task = asyncTaskService.createTask();
        demoAsyncTaskProcessor.processGraphSave(task.taskId(), request);
        return task;
    }

    public AsyncTaskStatusResponseDto getTaskStatus(String taskId) {
        return asyncTaskService.getTaskStatus(taskId);
    }
}
