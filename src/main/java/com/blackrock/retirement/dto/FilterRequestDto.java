package com.blackrock.retirement.dto;

import java.util.List;

public class FilterRequestDto {
    private List<QPeriodDto> q;
    private List<PPeriodDto> p;
    private List<KPeriodDto> k;
    private List<TransactionDto> transactions;

    public List<QPeriodDto> getQ() {
        return q;
    }

    public void setQ(List<QPeriodDto> q) {
        this.q = q;
    }

    public List<PPeriodDto> getP() {
        return p;
    }

    public void setP(List<PPeriodDto> p) {
        this.p = p;
    }

    public List<KPeriodDto> getK() {
        return k;
    }

    public void setK(List<KPeriodDto> k) {
        this.k = k;
    }

    public List<TransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionDto> transactions) {
        this.transactions = transactions;
    }
}
