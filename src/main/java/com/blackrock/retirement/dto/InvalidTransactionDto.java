package com.blackrock.retirement.dto;

/**
 * Transaction with validation error message for validator and filter endpoints.
 */
public class InvalidTransactionDto extends TransactionDto {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
