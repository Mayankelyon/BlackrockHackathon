package com.blackrock.retirement.dto;

import java.util.List;

public class ParseRequestDto {
    private List<ExpenseInput> expenses;

    public List<ExpenseInput> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<ExpenseInput> expenses) {
        this.expenses = expenses;
    }
}
