package com.bank.MaverickBank.dto.response;

import java.util.List;

public record AccountFilterDto(
        List<AccountRespDto> list,
        PaginationDto paginationInfo
) {
}
