package com.bank.MaverickBank.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BeneficiaryReqDto(
        @NotNull(message = "Beneficiary Account ID is required")
        @Positive(message = "Beneficiary Account ID should be Positive")
        Long beneficiaryAccountId,
        String beneficiaryName
) {
}
