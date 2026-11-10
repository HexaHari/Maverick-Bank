package com.bank.MaverickBank.dto.request;

import com.bank.MaverickBank.enums.Role;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(
        String password,
        Role role,
        Boolean isActive
) {
}
