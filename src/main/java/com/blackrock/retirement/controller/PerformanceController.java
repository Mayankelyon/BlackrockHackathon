package com.blackrock.retirement.controller;

import com.blackrock.retirement.dto.PerformanceResponseDto;
import com.blackrock.retirement.service.PerformanceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping(value = "/performance", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PerformanceResponseDto> performance() {
        PerformanceResponseDto report = performanceService.report();
        return ResponseEntity.ok(report);
    }
}
