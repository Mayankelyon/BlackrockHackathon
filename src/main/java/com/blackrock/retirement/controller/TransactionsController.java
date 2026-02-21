package com.blackrock.retirement.controller;

import com.blackrock.retirement.dto.*;
import com.blackrock.retirement.service.TransactionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class TransactionsController {

    private final TransactionService transactionService;

    public TransactionsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Transaction Builder: receives a list of Expenses, returns list of transactions with ceiling and remanent.
     * Input: list of Expenses — [ { "timestamp": "YYYY-MM-DD HH:mm:ss", "amount": number }, ... ]
     * Output: list of { date, amount, ceiling, remanent }.
     */
    @PostMapping(value = "/transactions:parse", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TransactionDto>> parse(@RequestBody(required = false) List<ExpenseInput> expenses) {
        List<ExpenseInput> list = expenses != null ? expenses : List.of();
        List<TransactionDto> result = transactionService.parse(list);
        return ResponseEntity.ok(result);
    }

    /**
     * Transaction Validator: valid, invalid, duplicate by wage and max amount to invest.
     */
    @PostMapping(value = "/transactions:validator", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValidatorResponseDto> validator(@RequestBody(required = false) ValidatorRequestDto body) {
        if (body == null) body = new ValidatorRequestDto();
        double wage = body.getWage();
        double max = body.getMaxAmountToInvest() > 0 ? body.getMaxAmountToInvest() : 500_000;
        List<TransactionDto> transactions = body.getTransactions() != null ? body.getTransactions() : List.of();
        ValidatorResponseDto result = transactionService.validate(wage, max, transactions);
        return ResponseEntity.ok(result);
    }

    /**
     * Temporal Constraints Validator: apply q, p, k and return valid/invalid transactions.
     */
    @PostMapping(value = "/transactions:filter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FilterResponseDto> filter(@RequestBody(required = false) FilterRequestDto body) {
        if (body == null) body = new FilterRequestDto();
        List<QPeriodDto> q = body.getQ() != null ? body.getQ() : List.of();
        List<PPeriodDto> p = body.getP() != null ? body.getP() : List.of();
        List<KPeriodDto> k = body.getK() != null ? body.getK() : List.of();
        List<TransactionDto> transactions = body.getTransactions() != null ? body.getTransactions() : List.of();
        FilterResponseDto result = transactionService.filter(q, p, k, transactions);
        return ResponseEntity.ok(result);
    }
}
