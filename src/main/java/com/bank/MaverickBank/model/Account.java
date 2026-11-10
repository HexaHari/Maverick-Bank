package com.bank.MaverickBank.model;

import com.bank.MaverickBank.enums.AccountType;
import com.bank.MaverickBank.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,name = "account_type")
    private AccountType accountType;
    @Column(precision = 15, scale = 2,nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    @Column(name = "transaction_pin", nullable = false)
    private String transactionPin;
    @Column(name = "is_active")
    private boolean isActive = false;
    @Enumerated(EnumType.STRING)
    @Column(name = "request_status",nullable = false)
    private RequestStatus requestStatus;
    @Column(name = "request_reason")
    private String requestReason;
    @CreationTimestamp
    @Column(name = "requested_at")
    private LocalDate requestedAt;
    @CreationTimestamp
    @Column(name = "created_at",updatable = false)
    private LocalDate createdAt;
    @Column(name = "activated_at")
    private LocalDate activatedAt;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Branch branch;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Customer customer;
    @ManyToOne
    @JoinColumn(name = "verified_employee_id")
    private Employee employee;
}
