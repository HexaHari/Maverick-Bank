package com.bank.MaverickBank.dto.request;

import com.bank.MaverickBank.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccountOpenReqDto(
        @NotNull(message = "Account Type is required")
        AccountType accountType,
        String requestReason,
        @NotBlank(message = "IFSC Code is required")
        @NotNull(message = "IFSC Code is required")
        @Pattern(
                regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
                message = "Invalid IFSC format. It must be 11 characters (e.g., SBIN0000300)."
        )
        String ifscCode,
        @NotBlank(message = "Transaction PIN is required")
        @NotNull(message = "Transaction PIN is required")
        @Pattern(
                regexp = "^[0-9]{4}$",
                message = "Invalid PIN format. It must be 4 Numbers(e.g., 1234)."
        )
        String transactionPin
) {
}
