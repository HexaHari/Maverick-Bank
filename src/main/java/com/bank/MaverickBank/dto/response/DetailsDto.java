package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.enums.Role;

public record DetailsDto(
        String username,
        Boolean isActive,
        Role role
) {
}
