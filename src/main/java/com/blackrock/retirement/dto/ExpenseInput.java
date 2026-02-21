package com.blackrock.retirement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Input expense for parse endpoint. Uses "timestamp" per API spec.
 */
public class ExpenseInput {
    private String timestamp;
    private double amount;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
