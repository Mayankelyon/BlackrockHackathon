package com.blackrock.retirement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Transaction with date, amount, ceiling, remanent. Output of parse; used in validator/filter/returns.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDto {
    private String date;   // "YYYY-MM-DD HH:mm:ss"
    private Double amount;
    private Double ceiling;
    private Double remanent;
    // For filter/validator: timestamp used in request body
    private String timestamp;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getCeiling() {
        return ceiling;
    }

    public void setCeiling(Double ceiling) {
        this.ceiling = ceiling;
    }

    public Double getRemanent() {
        return remanent;
    }

    public void setRemanent(Double remanent) {
        this.remanent = remanent;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
