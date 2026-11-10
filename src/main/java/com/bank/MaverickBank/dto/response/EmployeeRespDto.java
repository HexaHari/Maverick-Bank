package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.enums.JobTitle;

public record EmployeeRespDto(
        Long employeeId,
        String name,
        String contact,
        JobTitle jobTitle,
        String branchName,
        String ifscCode,
        String username,
        Boolean isActive
) {
}
