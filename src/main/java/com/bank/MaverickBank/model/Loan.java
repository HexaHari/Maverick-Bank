package com.bank.MaverickBank.model;

import com.bank.MaverickBank.enums.LoanStatus;
import com.bank.MaverickBank.enums.LoanType;
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
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,name = "loan_type")
    private LoanType loanType;
    @Column(precision = 15,scale = 2,nullable = false,name = "requested_amount")
    private BigDecimal requestedAmount;
    @Column(precision = 15,scale = 2,name = "approved_amount")
    private BigDecimal approvedAmount;
    @Column(precision = 5,scale = 2,name = "interest_rate")
    private BigDecimal interestRate;
    @Column(name = "tenure_months")
    private Integer tenureMonths;
    @Column(precision = 12,scale = 2)
    private BigDecimal emi;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,name="loan_status")
    private LoanStatus loanStatus;
    @Column(name = "verification_document")
    private String verificationDocument;
    @Column(name = "financial_verification")
    private boolean financialVerification = false;
    @Column(name = "disbursed_at")
    private LocalDate disbursedAt;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;
    @Column(name = "closed_at")
    private LocalDate closedAt;

    @ManyToOne
    @JoinColumn(name = "loan_branch_id")
    private Branch loanBranch;
    @ManyToOne
    @JoinColumn(name = "target_account_id")
    private Account targetAccount;
    @ManyToOne
    @JoinColumn(name = "managing_employee_id")
    private Employee managingEmployee;
}
