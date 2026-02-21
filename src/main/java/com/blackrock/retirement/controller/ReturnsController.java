package com.blackrock.retirement.controller;

import com.blackrock.retirement.dto.ReturnsRequestDto;
import com.blackrock.retirement.dto.ReturnsResponseDto;
import com.blackrock.retirement.service.TransactionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class ReturnsController {

    private final TransactionService transactionService;

    public ReturnsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(value = "/returns:nps", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReturnsResponseDto> returnsNps(@RequestBody(required = false) ReturnsRequestDto body) {
        if (body == null) body = new ReturnsRequestDto();
        ReturnsResponseDto result = transactionService.returnsNps(body);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/returns:index", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReturnsResponseDto> returnsIndex(@RequestBody(required = false) ReturnsRequestDto body) {
        if (body == null) body = new ReturnsRequestDto();
        ReturnsResponseDto result = transactionService.returnsIndex(body);
        return ResponseEntity.ok(result);
    }
}
