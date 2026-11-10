package com.bank.MaverickBank.dto.request;

import com.bank.MaverickBank.enums.LoanStatus;

public record LoanStatusStatDto(
        LoanStatus loanStatus,
        Long numberOfLoans
) {
}
