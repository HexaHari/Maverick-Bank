package com.bank.MaverickBank.dto.response;

import java.util.List;

public record EmployeeFilterDto(
        List<EmployeeRespDto> employeeList,
        PaginationDto paginationInfo
) {
}
