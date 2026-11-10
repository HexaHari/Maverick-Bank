package com.bank.MaverickBank.dto.request;

import com.bank.MaverickBank.enums.Gender;
import com.bank.MaverickBank.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CustomerReqDto(
        @NotNull(message = "Name is Required")
        @NotBlank(message = "Name is Required")
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
        String name,
        @NotNull(message = "Date of Birth is Required")
        LocalDate dateOfBirth,
        @NotNull(message = "Gender is Required")
        Gender gender,
        @NotNull(message = "Contact is Required")
        @NotBlank(message = "Contact is Required")
        @Pattern(regexp = "^[6-9][0-9]{9}$")
       // @Size(min = 10,max = 10,message = "Contact should be a 10 Digit number ")
        String contact,
        @NotNull(message = "Address is Required")
        @NotBlank(message = "Address is Required")
        @Size(max=200, message ="Address is too long")
        String address,
        @NotNull(message = "Aadhar Number is Required")
        @NotBlank(message = "Aadhar Number is Required")
        @Pattern(regexp = "^[2-9][0-9]{3}\\s?[0-9]{4}\\s?[0-9]{4}$",
                message = "Invalid Aadhaar number format.")
        String aadharNumber,
        @Pattern(
                regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$",
                message = "Invalid PAN number format. It must be 10 characters (e.g., ABCDE1234F)."
        )
        String panNumber,
        @NotNull(message = "Username is Required")
        @NotBlank(message = "Username is Required")
        @Size(min=3,max=25)
        String username,
        @NotNull(message = "Password is Required")
        @NotBlank(message = "Password is Required")
        String password
) {
}
