package com.bank.MaverickBank.dto.response;

import java.time.LocalDate;

public record BeneficiaryFilterRespDto(
        Long beneficiaryId,
        Long beneficiaryAccountId,
        String beneficiaryName,
        String beneficiaryCustomerName,
        String beneficiaryIfscCode,
        String beneficiaryBranchName,
        Long customerId,
        String customerName,
        LocalDate addedAt
) {
}
