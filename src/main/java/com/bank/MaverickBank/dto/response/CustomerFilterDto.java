package com.bank.MaverickBank.dto.response;

import java.util.List;

public record CustomerFilterDto(
        List<CustomerFilterRespDto> customerList,
        PaginationDto paginationInfo
) {
}
