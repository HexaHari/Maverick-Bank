package com.bank.MaverickBank.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LoanApproveReqDto(
        @NotNull(message = "Loan ID is required")
        @Positive(message = "Loan ID should be positive")
        Long loanId,
        @NotNull(message = "Approved Amount is required")
        @Positive(message = "Approved Amount should be positive")
        @Digits(integer = 13, fraction = 2)
        BigDecimal approvedAmount,
        @NotNull(message = "Interest Rate is required")
        @Positive(message = "Interest Rate should be positive")
        @DecimalMax(value = "100.00", message = "Interest Rate cannot be more than 100")
        @Digits(integer = 3, fraction = 2)
        BigDecimal interestRate
) {
}
