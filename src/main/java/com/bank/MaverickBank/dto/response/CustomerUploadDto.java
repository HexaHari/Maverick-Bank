package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.enums.Role;

public record CustomerUploadDto(
        String token,
        Role role
) {
}
