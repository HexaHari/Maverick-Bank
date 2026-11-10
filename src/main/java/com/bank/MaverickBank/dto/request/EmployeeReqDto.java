package com.bank.MaverickBank.dto.request;

import com.bank.MaverickBank.enums.JobTitle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmployeeReqDto(
           @NotNull(message = "Name is Required")
           @NotBlank(message = "Name is Required")
           String name,
           @NotNull(message = "Contact is Required")
           @NotBlank(message = "Contact is Required")
           @Pattern(regexp = "^[6-9][0-9]{9}$")
           String contact,
           @NotNull(message = "Job Title is required")
           JobTitle jobTitle,
           @NotBlank(message = "IFSC Code is required")
           @NotNull(message = "IFSC Code is required")
           @Pattern(
                   regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
                   message = "Invalid IFSC format. It must be 11 characters (e.g., SBIN0000300)."
           )
           String ifscCode,
           @NotNull(message = "Username is Required")
           @NotBlank(message = "Username is Required")
           @Size(min=3,max=25)
           String username,
           @NotNull(message = "Password is Required")
           @NotBlank(message = "Password is Required")
           String password
) {
}
