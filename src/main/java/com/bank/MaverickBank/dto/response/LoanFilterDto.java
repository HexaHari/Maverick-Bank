package com.bank.MaverickBank.dto.response;

import java.util.List;

public record LoanFilterDto(
        List<LoanRespDto> loanList,
        PaginationDto paginationInfo
) {
}
