package com.bank.MaverickBank.dto.request;

import com.bank.MaverickBank.enums.LoanType;

public record LoanTypeStatDto(
        LoanType loanType,
        Long numberOfLoans
) {
}
