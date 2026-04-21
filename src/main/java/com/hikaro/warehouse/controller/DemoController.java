package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.AsyncTaskStatusResponseDto;
import com.hikaro.warehouse.dto.AsyncTaskSubmissionResponseDto;
import com.hikaro.warehouse.dto.BulkOperationRequestDto;
import com.hikaro.warehouse.dto.CounterStateResponseDto;
import com.hikaro.warehouse.dto.ProductRequestDto;
import com.hikaro.warehouse.dto.RaceConditionDemoResponseDto;
import com.hikaro.warehouse.exception.ApiErrorResponse;
import com.hikaro.warehouse.service.ConcurrencyDemoService;
import com.hikaro.warehouse.service.DemoAsyncService;
import com.hikaro.warehouse.service.DemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@Tag(
        name = "Transactions Demo",
        description = "Endpoints demonstrating transactional and non-transactional bulk operations"
)
public class DemoController {

    private final DemoService demoService;
    private final DemoAsyncService demoAsyncService;
    private final ConcurrencyDemoService concurrencyDemoService;

    public DemoController(
            DemoService demoService,
            DemoAsyncService demoAsyncService,
            ConcurrencyDemoService concurrencyDemoService
    ) {
        this.demoService = demoService;
        this.demoAsyncService = demoAsyncService;
        this.concurrencyDemoService = concurrencyDemoService;
    }

    @PostMapping("/without-transaction")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Run bulk save without transaction")
    @ApiResponse(responseCode = "200", description = "Operation completed")
    @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Intentional failure after partial save",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public void withoutTransaction(
            @Valid @RequestBody BulkOperationRequestDto request
    ) {
        demoService.saveGraphWithoutTransaction(request);
    }

    @PostMapping("/with-transaction")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Run bulk save with transaction")
    @ApiResponse(responseCode = "200", description = "Operation completed")
    @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(
            responseCode = "500",
            description = "Intentional failure with transaction rollback",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public void withTransaction(
            @Valid @RequestBody BulkOperationRequestDto request
    ) {
        demoService.saveGraphWithTransaction(request);
    }

    @PostMapping("/products/without-transaction")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Run product bulk save without transaction")
    @ApiResponse(responseCode = "200", description = "Operation completed")
    @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(
            responseCode = "500",
            description = "Intentional failure after partial bulk product save",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public void bulkProductsWithoutTransaction(
            @Valid @RequestBody @NotEmpty(message = "Product list must not be empty")
            List<@Valid ProductRequestDto> requests
    ) {
        demoService.saveProductsBulkWithoutTransaction(requests);
    }

    @PostMapping("/products/with-transaction")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Run product bulk save with transaction")
    @ApiResponse(responseCode = "200", description = "Operation completed")
    @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Intentional failure with bulk rollback",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public void bulkProductsWithTransaction(
            @Valid @RequestBody @NotEmpty(message = "Product list must not be empty")
            List<@Valid ProductRequestDto> requests
    ) {
        demoService.saveProductsBulkWithTransaction(requests);
    }

    @PostMapping("/async/with-transaction")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Run graph save asynchronously with transaction")
    @ApiResponse(responseCode = "202", description = "Async task accepted")
    @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public AsyncTaskSubmissionResponseDto startAsyncWithTransaction(
            @Valid @RequestBody BulkOperationRequestDto request
    ) {
        return demoAsyncService.startGraphSaveWithTransaction(request);
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Get asynchronous task execution status")
    @ApiResponse(responseCode = "200", description = "Task status returned")
    @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public AsyncTaskStatusResponseDto getTaskStatus(@PathVariable String taskId) {
        return demoAsyncService.getTaskStatus(taskId);
    }

    @PostMapping("/counter/increment/synchronized")
    @Operation(summary = "Increment synchronized demo counter")
    @ApiResponse(responseCode = "200", description = "Counter incremented")
    public CounterStateResponseDto incrementSynchronizedCounter() {
        return concurrencyDemoService.incrementSynchronizedCounter();
    }

    @PostMapping("/counter/increment/atomic")
    @Operation(summary = "Increment atomic demo counter")
    @ApiResponse(responseCode = "200", description = "Counter incremented")
    public CounterStateResponseDto incrementAtomicCounter() {
        return concurrencyDemoService.incrementAtomicCounter();
    }

    @GetMapping("/counter")
    @Operation(summary = "Get current thread-safe counter values")
    @ApiResponse(responseCode = "200", description = "Counter values returned")
    public CounterStateResponseDto getCounterState() {
        return concurrencyDemoService.getCounterState();
    }

    @PostMapping("/counter/reset")
    @Operation(summary = "Reset demo counters")
    @ApiResponse(responseCode = "200", description = "Counters reset")
    public CounterStateResponseDto resetCounters() {
        return concurrencyDemoService.resetCounters();
    }

    @PostMapping("/race-condition")
    @Operation(summary = "Demonstrate race condition and thread-safe solutions")
    @ApiResponse(responseCode = "200", description = "Race condition demo completed")
    @ApiResponse(responseCode = "400", description = "Invalid demo parameters",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public RaceConditionDemoResponseDto runRaceConditionDemo(
            @RequestParam(defaultValue = "64") int threadCount,
            @RequestParam(defaultValue = "2000") int incrementsPerThread
    ) {
        return concurrencyDemoService.runRaceConditionDemo(
                threadCount,
                incrementsPerThread
        );
    }
}
