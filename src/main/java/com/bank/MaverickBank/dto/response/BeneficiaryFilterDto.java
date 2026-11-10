package com.bank.MaverickBank.dto.response;

import java.util.List;

public record BeneficiaryFilterDto(
        List<BeneficiaryFilterRespDto> beneficiaryList,
        PaginationDto paginationInfo
) {
}
