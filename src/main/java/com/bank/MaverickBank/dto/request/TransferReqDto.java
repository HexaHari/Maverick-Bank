package com.bank.MaverickBank.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransferReqDto(
        @NotNull(message = "Source Account Id is required")
        @Positive(message = "Source Account Id should be a valid Positive Number")
        Long sourceAccountId,
        @NotNull(message = "Destination Account Id is required")
        @Positive(message = "Destination Account Id should be a valid Positive Number")
        Long destinationAccountId,
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount should be a valid Positive Number")
        @Digits(integer = 13, fraction = 2, message = "Amount could only be 13 digits before and 2 digits after the decimal point")
        BigDecimal amount,
        @NotBlank(message = "Transaction PIN is required")
        @NotNull(message = "Transaction PIN is required")
        @Pattern(
                regexp = "^[0-9]{4}$",
                message = "Invalid PIN format. It must be 4 Numbers(e.g., 1234)."
        )
        String transactionPin
) {
}
