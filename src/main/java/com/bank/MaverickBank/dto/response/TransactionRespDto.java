package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.enums.TransactionType;
import com.bank.MaverickBank.model.Account;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionRespDto(
        Long transactionId,
        Long sourceAccountId,
        Long sourceCustomerId,
        String sourceCustomerName,
        Long destinationAccountId,
        Long destinationCustomerId,
        String destinationCustomerName,
        BigDecimal amount,
        TransactionType transactionType,
        BigDecimal sourceAccountBalance,
        BigDecimal destinationAccountBalance,
        Boolean isCompleted,
        Instant completedAt
) {
}
