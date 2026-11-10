package com.bank.MaverickBank.dto.response;

import java.util.List;

public record UserFilterDto(
        List<UserRespDto> list,
        PaginationDto paginationInfo
) {
}
