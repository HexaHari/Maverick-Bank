package com.bank.MaverickBank.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Beneficiary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,name = "beneficiary_name")
    private String beneficiaryName;
    @ManyToOne
    @JoinColumn(nullable = false,name = "beneficiary_account_id")
    private Account beneficiaryAccount;
    @CreationTimestamp
    @Column(name = "added_at")
    private LocalDate addedAt;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Customer customer;

}
