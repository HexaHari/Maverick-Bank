package com.bank.MaverickBank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminReqDto(
        @NotNull(message = "Username is Required")
        @NotBlank(message = "Username is Required")
        @Size(min=3,max=25)
        String username,
        @NotNull(message = "Password is Required")
        @NotBlank(message = "Password is Required")
        String password
) {
}
