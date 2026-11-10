package com.bank.MaverickBank.repository;

import com.bank.MaverickBank.dto.request.LoanStatusStatDto;
import com.bank.MaverickBank.dto.request.LoanTypeStatDto;
import com.bank.MaverickBank.dto.response.LoanRespDto;
import com.bank.MaverickBank.enums.LoanStatus;
import com.bank.MaverickBank.enums.LoanType;
import com.bank.MaverickBank.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("""
            Select l.id as loanId, ta.id as targetAccountId, c.id as customerId, c.name as customerName, l.loanType as loanType,
            l.requestedAmount as requestedAmount, l.approvedAmount as approvedAmount, l.tenureMonths as tenureMonths,
            l.interestRate as interestRate, l.emi as emi, l.loanStatus as loanStatus,
            l.verificationDocument as verificationDocument, l.financialVerification as financialVerification, lb.id as loanBranchId,
            lb.ifscCode as loanBranchIfscCode, me.id as managingEmployeeId, me.name as managingEmployeeName,l.createdAt as createdAt,
            l.disbursedAt as disbursedAt, l.closedAt as closedAt
            From Loan l
            Left Join l.targetAccount ta
            Left Join ta.customer c
            Left Join l.managingEmployee me
            Left Join l.loanBranch lb
            Where l.id = ?1
            """)
    LoanRespDto getLoanById(Long loanId);

    @Query("""
        Select l.id as loanId, ta.id as targetAccountId, c.id as customerId, c.name as customerName, l.loanType as loanType,
        l.requestedAmount as requestedAmount, l.approvedAmount as approvedAmount, l.tenureMonths as tenureMonths,
        l.interestRate as interestRate, l.emi as emi, l.loanStatus as loanStatus,l.verificationDocument as verificationDocument,
        l.financialVerification as financialVerification, lb.id as loanBranchId,lb.ifscCode as loanBranchIfscCode, 
        me.id as managingEmployeeId, me.name as managingEmployeeName, l.createdAt as createdAt,
        l.disbursedAt as disbursedAt, l.closedAt as closedAt
        From Loan l
        Left Join l.targetAccount ta
        Left Join ta.customer c
        Left Join l.managingEmployee me
        Left Join l.loanBranch lb
        Where (?1 is Null OR l.id = ?1)
        AND (?2 is Null OR ta.id = ?2)
        AND (?3 is Null OR c.id = ?3)
        AND (?4 is Null OR l.loanType = ?4)
        AND (?5 is Null OR l.loanStatus = ?5)
        AND (?6 is Null OR l.requestedAmount >= ?6)
        AND (?7 is Null OR l.requestedAmount <= ?7)
        AND (?8 is Null OR l.approvedAmount >= ?8)
        AND (?9 is Null OR l.approvedAmount <= ?9)
        AND (?10 is Null OR lb.ifscCode LIKE CONCAT('%', ?10, '%'))
        AND (?11 is Null OR me.id = ?11)
        AND (?12 is Null OR l.financialVerification = ?12)
        AND (?13 is Null OR l.createdAt >= ?13)
        AND (?14 is Null OR l.createdAt <= ?14)
        AND (?15 is Null OR l.disbursedAt >= ?15)
        AND (?16 is Null OR l.disbursedAt <= ?16)
        AND (?17 is Null OR l.closedAt >= ?17)
        AND (?18 is Null OR l.closedAt <= ?18)
        """)
    Page<LoanRespDto> dynamicFilterLoan(Long loanId, Long targetAccountId, Long customerId, LoanType loanType,
                                        LoanStatus loanStatus,
                                        BigDecimal minRequestedAmount, BigDecimal maxRequestedAmount,
                                        BigDecimal minApprovedAmount, BigDecimal maxApprovedAmount,
                                        String ifscCode, Long managingEmployeeId, Boolean financialVerification,
                                        LocalDate createdFrom, LocalDate createdTill,
                                        LocalDate disbursedFrom, LocalDate disbursedTill,
                                        LocalDate closedFrom, LocalDate closedTill, Pageable pageable);

    @Query("""
        select l.loanStatus as loanStatus, count(l.id) as numberOfLoans
        from Loan l
        where l.loanBranch.id = ?1
        group by l.loanStatus
        """)
    List<LoanStatusStatDto> getLoanStatusStats(Long branchId);

    @Query("""
        select l.loanType as loanType, count(l.id) as numberOfLoans
        from Loan l
        where l.loanBranch.id = ?1
        group by l.loanType
        """)
    List<LoanTypeStatDto> getLoanTypeStats(Long branchId);
}
