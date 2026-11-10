package com.bank.MaverickBank.dto.response;

import java.time.LocalDate;

public record BranchRespDto(
        Long branchId,
        String branchName,
        String ifscCode,
        LocalDate addedAt
) {
}
