package com.bank.MaverickBank.dto.request;

import com.bank.MaverickBank.enums.LoanType;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record LoanOpenReqDto(
        @NotNull(message = "Target Account ID is required")
        @Positive(message = "Target Account ID should be positive")
        Long targetAccountId,
        @NotNull(message = "Loan Type is required")
        LoanType loanType,
        @NotNull(message = "Requested Amount is required")
        @Positive(message = "Requested Amount should be positive")
        @DecimalMin(value = "5000.00", message = "Minimum loan amount is 5000")
        @Digits(integer = 13, fraction = 2)
        BigDecimal requestedAmount,
        @NotNull(message = "Tenure is required")
        @Positive(message = "Tenure must be strictly positive")
        @Min(value = 6, message = "Minimum loan tenure is 6 months")
        Integer tenureMonths,
        @NotBlank(message = "IFSC Code is required")
        @NotNull(message = "IFSC Code is required")
        @Pattern(
                regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
                message = "Invalid IFSC format. It must be 11 characters (e.g., MAVB0000300)."
        )
        String ifscCode,
        MultipartFile verificationDocument
) {
}
