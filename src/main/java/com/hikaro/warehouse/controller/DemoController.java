package com.hikaro.warehouse.controller;

import com.hikaro.warehouse.dto.BulkOperationRequestDto;
import com.hikaro.warehouse.service.DemoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @PostMapping("/without-transaction")
    @ResponseStatus(HttpStatus.OK)
    public void withoutTransaction(
            @RequestBody BulkOperationRequestDto request
    ) {
        demoService.saveGraphWithoutTransaction(request);
    }

    @PostMapping("/with-transaction")
    @ResponseStatus(HttpStatus.OK)
    public void withTransaction(
            @RequestBody BulkOperationRequestDto request
    ) {
        demoService.saveGraphWithTransaction(request);
    }
}
