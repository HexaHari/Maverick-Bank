package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.enums.Gender;

import java.time.LocalDate;

public record CustomerFilterRespDto(
        Long customerId,
        String name,
        LocalDate dateOfBirth,
        Gender gender,
        String contact,
        String address,
        String aadharNumber,
        String panNumber,
        String picture,
        String identificationDoc
) {
}
