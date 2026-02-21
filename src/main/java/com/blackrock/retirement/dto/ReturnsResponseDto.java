package com.blackrock.retirement.dto;

import java.util.List;

/**
 * Response for returns:nps and returns:index endpoints.
 */
public class ReturnsResponseDto {
    private double transactionsTotalAmount;
    private double transactionsTotalCeiling;
    private List<SavingsByDatesDto> savingsByDates;

    public double getTransactionsTotalAmount() {
        return transactionsTotalAmount;
    }

    public void setTransactionsTotalAmount(double transactionsTotalAmount) {
        this.transactionsTotalAmount = transactionsTotalAmount;
    }

    public double getTransactionsTotalCeiling() {
        return transactionsTotalCeiling;
    }

    public void setTransactionsTotalCeiling(double transactionsTotalCeiling) {
        this.transactionsTotalCeiling = transactionsTotalCeiling;
    }

    public List<SavingsByDatesDto> getSavingsByDates() {
        return savingsByDates;
    }

    public void setSavingsByDates(List<SavingsByDatesDto> savingsByDates) {
        this.savingsByDates = savingsByDates;
    }
}
