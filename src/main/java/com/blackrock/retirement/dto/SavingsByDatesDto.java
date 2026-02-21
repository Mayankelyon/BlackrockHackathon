package com.blackrock.retirement.dto;

/**
 * Per-k-period result: start, end, amount, profits, taxBenefit (0 for index).
 */
public class SavingsByDatesDto {
    private String start;
    private String end;
    private double amount;
    private double profits;
    private double taxBenefit;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getProfits() {
        return profits;
    }

    public void setProfits(double profits) {
        this.profits = profits;
    }

    public double getTaxBenefit() {
        return taxBenefit;
    }

    public void setTaxBenefit(double taxBenefit) {
        this.taxBenefit = taxBenefit;
    }
}
