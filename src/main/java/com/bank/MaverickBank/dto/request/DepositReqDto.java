package com.bank.MaverickBank.dto.request;


import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DepositReqDto(
        @NotNull(message = "Destination Account Id is required")
        @Positive(message = "Destination Account Id should be a valid Positive Number")
        Long destinationAccountId,
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount should be a valid Positive Number")
        @Digits(integer = 13, fraction = 2, message = "Amount could only be 13 digits before and 2 digits after the decimal point")
        BigDecimal amount
) {
}
