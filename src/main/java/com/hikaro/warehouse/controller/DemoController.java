package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.BulkOperationRequestDto;
import com.hikaro.warehouse.dto.ProductRequestDto;
import com.hikaro.warehouse.exception.ApiErrorResponse;
import com.hikaro.warehouse.service.DemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@Tag(name = "Transactions Demo", description = "Endpoints demonstrating transactional and non-transactional bulk operations")
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @PostMapping("/without-transaction")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Run bulk save without transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation completed"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Intentional failure after partial save",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void withoutTransaction(
            @Valid @RequestBody BulkOperationRequestDto request
    ) {
        demoService.saveGraphWithoutTransaction(request);
    }

    @PostMapping("/with-transaction")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Run bulk save with transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation completed"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Intentional failure with transaction rollback",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void withTransaction(
            @Valid @RequestBody BulkOperationRequestDto request
    ) {
        demoService.saveGraphWithTransaction(request);
    }

    @PostMapping("/products/without-transaction")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Run product bulk save without transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation completed"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Intentional failure after partial bulk product save",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void bulkProductsWithoutTransaction(
            @Valid @RequestBody @NotEmpty(message = "Product list must not be empty")
            List<@Valid ProductRequestDto> requests
    ) {
        demoService.saveProductsBulkWithoutTransaction(requests);
    }

    @PostMapping("/products/with-transaction")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Run product bulk save with transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation completed"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Intentional failure with bulk rollback",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void bulkProductsWithTransaction(
            @Valid @RequestBody @NotEmpty(message = "Product list must not be empty")
            List<@Valid ProductRequestDto> requests
    ) {
        demoService.saveProductsBulkWithTransaction(requests);
    }
}
