package com.hikaro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current status of an asynchronous task")
public enum AsyncTaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}
