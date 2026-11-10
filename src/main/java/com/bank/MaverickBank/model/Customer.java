package com.bank.MaverickBank.model;

import com.bank.MaverickBank.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false,name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column(nullable = false,unique = true)
    private String contact;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false,unique = true,name = "aadhar_number")
    private String aadharNumber;
    @Column(unique = true,name = "pan_number")
    private String panNumber;
    private String picture;
    @Column(name = "identification_doc")
    private String identificationDoc;


    @OneToOne
    @JoinColumn(nullable = false)
    private User user;
}
