package com.bank.MaverickBank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BranchReqDto(
        @NotBlank(message = "Branch Name is required")
        @NotNull(message = "Branch Name is required")
        String branchName,
        @NotBlank(message = "IFSC Code is required")
        @NotNull(message = "IFSC Code is required")
        @Pattern(
                regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
                message = "Invalid IFSC format. It must be 11 characters (e.g., MAVB0000300)."
        )
        String ifscCode
) {
}
