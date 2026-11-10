package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.enums.Role;

import java.time.LocalDate;

public record UserRespDto(
        Long userId,
        String username,
        Role role,
        Boolean isActive,
        LocalDate createdAt,
        LocalDate updatedAt
) {
}
