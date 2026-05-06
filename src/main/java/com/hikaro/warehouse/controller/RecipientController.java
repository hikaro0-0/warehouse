package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.RecipientRequestDto;
import com.hikaro.warehouse.dto.RecipientResponseDto;
import com.hikaro.warehouse.entity.Recipient;
import com.hikaro.warehouse.exception.ApiErrorResponse;
import com.hikaro.warehouse.service.RecipientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/recipients")
@Tag(name = "Recipients", description = "Dispatch recipient management endpoints")
public class RecipientController {

    private final RecipientService recipientService;

    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    @GetMapping
    @Operation(summary = "Get all recipients")
    @ApiResponse(
            responseCode = "200",
            description = "Recipients fetched successfully",
            content = @Content(
                    array = @ArraySchema(
                            schema = @Schema(implementation = RecipientResponseDto.class)
                    )
            )
    )
    public List<RecipientResponseDto> findAll() {
        return recipientService.findAll().stream().map(this::toResponseDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recipient by id")
    @ApiResponse(
            responseCode = "200",
            description = "Recipient found",
            content = @Content(schema = @Schema(implementation = RecipientResponseDto.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Recipient not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public RecipientResponseDto getById(@PathVariable Long id) {
        return toResponseDto(recipientService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create recipient")
    @ApiResponse(
            responseCode = "201",
            description = "Recipient created",
            content = @Content(schema = @Schema(implementation = RecipientResponseDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public RecipientResponseDto create(@Valid @RequestBody RecipientRequestDto request) {
        return toResponseDto(recipientService.create(toEntity(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update recipient")
    @ApiResponse(
            responseCode = "200",
            description = "Recipient updated",
            content = @Content(schema = @Schema(implementation = RecipientResponseDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Recipient not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public RecipientResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody RecipientRequestDto request
    ) {
        return toResponseDto(recipientService.update(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete recipient")
    @ApiResponse(responseCode = "204", description = "Recipient deleted")
    @ApiResponse(
            responseCode = "404",
            description = "Recipient not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    public void delete(@PathVariable Long id) {
        recipientService.delete(id);
    }

    private Recipient toEntity(RecipientRequestDto request) {
        return new Recipient(
                null,
                request.name(),
                request.type(),
                request.contactEmail(),
                request.address()
        );
    }

    private RecipientResponseDto toResponseDto(Recipient recipient) {
        return new RecipientResponseDto(
                recipient.getId(),
                recipient.getName(),
                recipient.getType(),
                recipient.getContactEmail(),
                recipient.getAddress()
        );
    }
}
