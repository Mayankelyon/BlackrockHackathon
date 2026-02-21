package com.blackrock.retirement.dto;

import java.util.List;

public class ValidatorRequestDto {
    private double wage;
    private double maxAmountToInvest = 500_000; // x < 5×10^5 per spec
    private List<TransactionDto> transactions;

    public double getWage() {
        return wage;
    }

    public void setWage(double wage) {
        this.wage = wage;
    }

    public double getMaxAmountToInvest() {
        return maxAmountToInvest;
    }

    public void setMaxAmountToInvest(double maxAmountToInvest) {
        this.maxAmountToInvest = maxAmountToInvest;
    }

    public List<TransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionDto> transactions) {
        this.transactions = transactions;
    }
}
