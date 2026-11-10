package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.enums.AccountType;
import com.bank.MaverickBank.enums.RequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountStatusRespDto(
        Long accountId,
        AccountType accountType,
        BigDecimal balance,
        Boolean isActive,
        RequestStatus requestStatus,
        Long customerId,
        String customerName,
        Long employeeId,
        String employeeName
) {
}
