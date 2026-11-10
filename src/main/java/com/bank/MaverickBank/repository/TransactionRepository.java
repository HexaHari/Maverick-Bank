package com.bank.MaverickBank.repository;

import com.bank.MaverickBank.dto.response.DepositRespDto;
import com.bank.MaverickBank.dto.response.TransactionRespDto;
import com.bank.MaverickBank.dto.response.TransferRespDto;
import com.bank.MaverickBank.dto.response.WithdrawRespDto;
import com.bank.MaverickBank.enums.TransactionType;
import com.bank.MaverickBank.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            Select t.id as transactionId, d.id as destinationAccountId, t.amount as amount, d.balance as balance,
            t.transactionType as transactionType, t.isCompleted as isCompleted, t.completedAt as completedAt
            from Transaction t
            Join t.destinationAccount d
            Where t.id = ?1
            """)
    DepositRespDto getDepositTransactionResp(Long transactionId);

    @Query("""
            Select t.id as transactionId, s.id as sourceAccountId, t.amount as amount, s.balance as balance,
            t.transactionType as transactionType, t.isCompleted as isCompleted, t.completedAt as completedAt
            from Transaction t
            Join t.sourceAccount s
            Where t.id = ?1
            """)
    WithdrawRespDto getWithdrawTransactionResp(Long transactionId);

    @Query("""
            Select t.id as transactionId, s.id as sourceAccountId, d.id as destinationAccountId, t.amount as amount,
            s.balance as balance, t.transactionType as transactionType, t.isCompleted as isCompleted, t.completedAt as completedAt
            from Transaction t
            Join t.sourceAccount s
            Join t.destinationAccount d
            Where t.id = ?1
            """)
    TransferRespDto getTransferTransactionResp(Long transactionId);

    @Query("""
            Select t.id as transactionId, sa.id as sourceAccountId, sc.id as sourceCustomerId, sc.name as sourceCustomerName,
            da.id as destinationAccountId, dc.id as destinationCustomerId, dc.name as destinationCustomerName, t.amount as amount,
            t.transactionType as transactionType, t.sourceAccountBalance as sourceAccountBalance, t.destinationAccountBalance as destinationAccountBalance,
            t.isCompleted as isCompleted, t.completedAt as completedAt
            from Transaction t
            Left Join t.sourceAccount sa
            Left Join sa.customer sc
            Left Join t.destinationAccount da
            Left Join da.customer dc
            Where (?1 is Null OR t.id = ?1)
            And (
            ( ?2 is Null And ?3 is Null)
            OR
            ( ?2 is not Null And ?3 is Null And sa.id = ?2)
            OR
            ( ?2 is Null And ?3 is Not Null And da.id = ?3)
            OR
            ( ?2 = ?3 And (sa.id = ?2 OR da.id = ?3))
            OR
            ( ?2 != ?3 And (sa.id = ?2 And da.id = ?3))
            )
            And (?4 is Null OR t.transactionType = ?4)
            And (?5 is Null OR t.amount >= ?5)
            And (?6 is Null OR t.amount <= ?6)
            And (?7 is Null OR t.isCompleted = ?7)
            And (?8 is Null OR t.completedAt >= ?8)
            And (?9 is Null OR t.completedAt < ?9)
            And (
            ( ?10 is Null And ?11 is Null)
            OR
            ( ?10 is not Null And ?11 is Null And sc.id = ?10)
            OR
            ( ?10 is Null And ?11 is Not Null And dc.id = ?11)
            OR
            ( ?10 = ?11 And (sc.id = ?10 OR dc.id = ?11))
            OR
            ( ?10 != ?11 And (sc.id = ?10 And dc.id = ?11))
            )
            """)
    Page<TransactionRespDto> dynamicFilterTransaction(Long transactionId, Long sourceAccountId, Long destinationAccountId,
                                                      TransactionType transactionType, BigDecimal minAmount, BigDecimal maxAmount,
                                                      Boolean isCompleted, Instant completedFrom, Instant completedTill,
                                                      Long sourceCustomerId, Long destinationCustomerId, Pageable pageable);
}
