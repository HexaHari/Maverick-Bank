package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.enums.RequestStatus;

public record AccountRequestStatusStatDto(
        RequestStatus requestStatus,
        Long numberOfAccounts
) {
}
