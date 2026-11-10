package com.bank.MaverickBank.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record CustomerUpdateDto(
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
        String name,
        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Contact is not in Correct 10 digit format")
        String contact,
        @Size(max=200, message ="Address is too long")
        String address,
        MultipartFile picture,
        MultipartFile identificationDoc
){
        }
