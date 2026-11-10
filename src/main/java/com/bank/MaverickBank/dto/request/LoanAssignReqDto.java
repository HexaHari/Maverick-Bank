package com.bank.MaverickBank.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoanAssignReqDto(
        @NotNull(message = "Loan ID is required")
        @Positive(message = "Loan ID should be positive")
        Long loanId,
        @NotNull(message = "Employee ID is required")
        @Positive(message = "Employee ID should be positive")
        Long employeeId
) {
}
