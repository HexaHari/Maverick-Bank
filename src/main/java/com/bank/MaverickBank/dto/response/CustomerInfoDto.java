package com.bank.MaverickBank.dto.response;

import com.bank.MaverickBank.enums.Gender;


import java.time.LocalDate;

public record CustomerInfoDto(
 Long id,
 String name,
 LocalDate dateOfBirth,
 Gender gender,
 String contact,
 String address,
 String picture,
 String identificationDoc
) {
}
