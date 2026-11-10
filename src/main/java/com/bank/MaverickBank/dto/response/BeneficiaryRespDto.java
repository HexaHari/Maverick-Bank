package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.model.Branch;

import java.time.LocalDate;

public record BeneficiaryRespDto(
        Long beneficiaryId,
        Long beneficiaryAccountId,
        String beneficiaryName,
        String beneficiaryBranchName,
        LocalDate addedAt,
        Long customerId
) {
}
