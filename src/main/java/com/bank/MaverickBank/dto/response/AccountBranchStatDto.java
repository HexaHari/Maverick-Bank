package com.bank.MaverickBank.dto.response;

public record AccountBranchStatDto(
        String branchName,
        Long numberOfAccounts
) {
}
