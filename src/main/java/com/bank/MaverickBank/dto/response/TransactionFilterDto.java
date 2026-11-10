package com.bank.MaverickBank.dto.response;

import java.util.List;

public record TransactionFilterDto(
        List<TransactionRespDto> transactionList,
        PaginationDto paginationInfo
) {
}
