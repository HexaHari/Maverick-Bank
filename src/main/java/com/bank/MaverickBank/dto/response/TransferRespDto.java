package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferRespDto(
        Long transactionId,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        BigDecimal balance,
        TransactionType transactionType,
        Boolean isCompleted,
        Instant completedAt
) {
}
