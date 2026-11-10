package com.bank.MaverickBank.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StatementRowDto(
        LocalDate date,
        String details,        // e.g. "Transfer to 1002 - Ravi", "Cheque deposit", "Cash withdrawal"
        BigDecimal debit,      // null for credit rows
        BigDecimal credit,     // null for debit rows
        BigDecimal balance     // running balance after this row
) {
}
