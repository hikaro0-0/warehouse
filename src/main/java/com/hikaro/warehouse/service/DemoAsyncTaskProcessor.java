package com.hikaro.warehouse.service;

import com.hikaro.warehouse.dto.BulkOperationRequestDto;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DemoAsyncTaskProcessor {

    private static final Logger log = LoggerFactory.getLogger(DemoAsyncTaskProcessor.class);

    private final AsyncTaskService asyncTaskService;
    private final DemoService demoService;

    public DemoAsyncTaskProcessor(
            AsyncTaskService asyncTaskService,
            DemoService demoService
    ) {
        this.asyncTaskService = asyncTaskService;
        this.demoService = demoService;
    }

    @Async("businessTaskExecutor")
    public CompletableFuture<Void> processGraphSave(
            String taskId,
            BulkOperationRequestDto request
    ) {
        asyncTaskService.markRunning(taskId);
        try {
            demoService.saveGraphForAsyncTask(request);
            asyncTaskService.markCompleted(taskId);
        } catch (Exception ex) {
            log.error("Async task {} failed", taskId, ex);
            asyncTaskService.markFailed(taskId, ex);
        }
        return CompletableFuture.completedFuture(null);
    }
}
