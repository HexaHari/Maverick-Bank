package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.enums.AccountType;
import com.bank.MaverickBank.enums.RequestStatus;
import com.bank.MaverickBank.model.Account;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountRespDto(
        Long accountId,
        AccountType accountType,
        BigDecimal balance,
        Boolean isActive,
        RequestStatus requestStatus,
        String requestReason,
        LocalDate requestedAt,
        LocalDate createdAt,
        LocalDate activatedAt,
        String branchName,
        String ifscCode,
        Long customerId,
        String customerName
) {
}
