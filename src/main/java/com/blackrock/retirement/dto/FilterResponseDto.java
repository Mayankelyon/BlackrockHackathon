package com.blackrock.retirement.dto;

import java.util.List;

public class FilterResponseDto {
    private List<TransactionDto> valid;
    private List<InvalidTransactionDto> invalid;

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
}
