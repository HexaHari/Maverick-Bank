package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.enums.LoanStatus;
import com.bank.MaverickBank.enums.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanRespDto(
        Long loanId,
        Long targetAccountId,
        Long customerId,
        String customerName,
        LoanType loanType,
        BigDecimal requestedAmount,
        BigDecimal approvedAmount,
        Integer tenureMonths,
        BigDecimal interestRate,
        BigDecimal emi,
        LoanStatus loanStatus,
        String verificationDocument,
        Boolean financialVerification,
        Long loanBranchId,
        String loanBranchIfscCode,
        Long managingEmployeeId,
        String managingEmployeeName,
        LocalDate createdAt,
        LocalDate disbursedAt,
        LocalDate closedAt
) {
}
