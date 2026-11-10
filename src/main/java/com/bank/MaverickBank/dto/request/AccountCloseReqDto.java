package com.bank.MaverickBank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AccountCloseReqDto(
        @NotNull(message = "Account ID is required")
        @Positive(message = "Account ID is a positive number")
        Long accountId,
        @NotNull(message = "Request Reason is required")
        @NotBlank(message = "Request Reason is required")
        String requestReason
) {
}
