package com.bank.MaverickBank.dto.response;

import java.util.List;

public record BranchFilterDto(
        List<BranchRespDto> list,
        PaginationDto paginationInfo
) {
}
