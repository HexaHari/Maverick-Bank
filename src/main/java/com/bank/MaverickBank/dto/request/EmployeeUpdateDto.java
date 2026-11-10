package com.bank.MaverickBank.dto.request;

import com.bank.MaverickBank.enums.JobTitle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmployeeUpdateDto(
        @Size(min = 3,message = "At min the name should be 3 characters")
        String name,
        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Contact must be 10 digit Number")
        String contact,
        JobTitle jobTitle,
        Long branchId,
        Long userId
) {
}
