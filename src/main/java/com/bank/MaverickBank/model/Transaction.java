package com.bank.MaverickBank.model;

import com.bank.MaverickBank.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @ManyToOne
    @JoinColumn(name = "source_account")
    private Account sourceAccount;

    @ManyToOne
    @JoinColumn(name = "destination_account")
    private Account destinationAccount;
    @Column(precision = 15,scale = 2,nullable = false)
    private BigDecimal amount;
    @Column(precision = 15,scale = 2)
    private BigDecimal sourceAccountBalance;
    @Column(precision = 15,scale = 2)
    private BigDecimal destinationAccountBalance;
    @Column(name = "cheque_image")
    private String chequeImage;
    @Column(name = "is_completed")
    private boolean isCompleted = false;

    @CreationTimestamp
    @Column(name = "completed_at")
    private Instant completedAt;

}
