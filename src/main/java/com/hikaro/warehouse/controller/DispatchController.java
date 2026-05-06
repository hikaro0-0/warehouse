package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.DispatchItemResponseDto;
import com.hikaro.warehouse.dto.DispatchRequestDto;
import com.hikaro.warehouse.dto.DispatchResponseDto;
import com.hikaro.warehouse.entity.Dispatch;
import com.hikaro.warehouse.entity.DispatchItem;
import com.hikaro.warehouse.exception.ApiErrorResponse;
import com.hikaro.warehouse.service.DispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dispatches")
@Tag(name = "Dispatches", description = "Outgoing dispatch management endpoints")
public class DispatchController {

    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @GetMapping
    @Operation(summary = "Get all dispatches")
    @ApiResponse(
            responseCode = "200",
            description = "Dispatches fetched successfully",
            content = @Content(
                    array = @ArraySchema(
                            schema = @Schema(implementation = DispatchResponseDto.class)
                    )
            )
    )
    public List<DispatchResponseDto> findAll() {
        return dispatchService.findAll().stream().map(this::toResponseDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get dispatch by id")
    @ApiResponse(
            responseCode = "200",
            description = "Dispatch found",
            content = @Content(schema = @Schema(implementation = DispatchResponseDto.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Dispatch not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public DispatchResponseDto getById(@PathVariable Long id) {
        return toResponseDto(dispatchService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create dispatch")
    @ApiResponse(
            responseCode = "201",
            description = "Dispatch created",
            content = @Content(schema = @Schema(implementation = DispatchResponseDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public DispatchResponseDto create(@Valid @RequestBody DispatchRequestDto request) {
        return toResponseDto(dispatchService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update dispatch")
    @ApiResponse(
            responseCode = "200",
            description = "Dispatch updated",
            content = @Content(schema = @Schema(implementation = DispatchResponseDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Dispatch not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public DispatchResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody DispatchRequestDto request
    ) {
        return toResponseDto(dispatchService.update(id, request));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm draft dispatch and deduct stock")
    @ApiResponse(
            responseCode = "200",
            description = "Dispatch confirmed",
            content = @Content(schema = @Schema(implementation = DispatchResponseDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Dispatch cannot be confirmed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Dispatch not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public DispatchResponseDto confirm(@PathVariable Long id) {
        return toResponseDto(dispatchService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel draft dispatch without deducting stock")
    @ApiResponse(
            responseCode = "200",
            description = "Dispatch cancelled",
            content = @Content(schema = @Schema(implementation = DispatchResponseDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Dispatch cannot be cancelled",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Dispatch not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public DispatchResponseDto cancel(@PathVariable Long id) {
        return toResponseDto(dispatchService.cancel(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete dispatch")
    @ApiResponse(responseCode = "204", description = "Dispatch deleted")
    @ApiResponse(
            responseCode = "404",
            description = "Dispatch not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public void delete(@PathVariable Long id) {
        dispatchService.delete(id);
    }

    private DispatchResponseDto toResponseDto(Dispatch dispatch) {
        return new DispatchResponseDto(
                dispatch.getId(),
                dispatch.getReferenceNumber(),
                dispatch.getWarehouse().getId(),
                dispatch.getWarehouse().getName(),
                dispatch.getRecipient().getId(),
                dispatch.getRecipient().getName(),
                dispatch.getRecipient().getType(),
                dispatch.getStatus(),
                dispatch.getCreatedAt(),
                dispatch.getUpdatedAt(),
                dispatch.getItems()
                        .stream()
                        .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                        .map(this::toItemResponseDto)
                        .toList()
        );
    }

    private DispatchItemResponseDto toItemResponseDto(DispatchItem item) {
        return new DispatchItemResponseDto(
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity()
        );
    }
}
