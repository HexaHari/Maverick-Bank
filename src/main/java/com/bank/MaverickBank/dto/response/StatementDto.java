package com.bank.MaverickBank.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StatementDto(
        String accountHolderName,
        String accountType,
        Long accountId,
        String address,
        String branchName,
        String ifscCode,
        String currency,       // "INR"
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal openingBalance,
        BigDecimal totalCredits,
        BigDecimal totalDebits,
        BigDecimal endingBalance,
        List<StatementRowDto> activity
) {
}
