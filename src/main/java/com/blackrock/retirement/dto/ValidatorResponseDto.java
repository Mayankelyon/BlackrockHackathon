package com.blackrock.retirement.dto;

import java.util.List;

public class ValidatorResponseDto {
    private List<TransactionDto> valid;
    private List<InvalidTransactionDto> invalid;
    private List<TransactionDto> duplicate;

    public List<TransactionDto> getValid() {
        return valid;
    }

    public void setValid(List<TransactionDto> valid) {
        this.valid = valid;
    }

    public List<InvalidTransactionDto> getInvalid() {
        return invalid;
    }

    public void setInvalid(List<InvalidTransactionDto> invalid) {
        this.invalid = invalid;
    }

    public List<TransactionDto> getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(List<TransactionDto> duplicate) {
        this.duplicate = duplicate;
    }
}
