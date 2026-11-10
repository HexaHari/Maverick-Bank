package com.bank.MaverickBank.repository;

import com.bank.MaverickBank.dto.response.AccountBranchStatDto;
import com.bank.MaverickBank.dto.response.AccountRequestStatusStatDto;
import com.bank.MaverickBank.dto.response.AccountRespDto;
import com.bank.MaverickBank.dto.response.AccountStatusRespDto;
import com.bank.MaverickBank.enums.AccountType;
import com.bank.MaverickBank.enums.RequestStatus;
import com.bank.MaverickBank.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {

    @Query("""
            Select a.id as accountId, a.accountType as accountType, a.balance as balance, a.isActive as isActive,
            a.requestStatus as requestStatus, a.requestReason as requestReason, a.requestedAt as requestedAt,
            a.createdAt as createdAt, a.activatedAt as activatedAt, b.branchName as branchName, b.ifscCode as ifscCode,
            c.id as customerId, c.name as customerName
            from Account a
            Join a.branch  b
            Join a.customer c
            Where (?1 is Null OR a.accountType = ?1)
            AND (?2 is Null OR a.balance >= ?2)
            AND (?3 is Null OR a.balance <= ?3)
            AND (?4 is Null OR a.isActive = ?4)
            AND (?5 is Null OR a.requestStatus = ?5)
            AND (?6 is Null OR a.createdAt >= ?6)
            AND (?7 is Null OR a.createdAt <= ?7)
            AND (?8 is Null OR a.activatedAt >= ?8)
            AND (?9 is Null OR a.activatedAt <= ?9)
            AND (?10 is Null OR b.ifscCode = ?10)
            AND (?11 is Null OR c.id = ?11)
            AND (?12 is Null OR a.id = ?12)
            AND (?13 is Null OR a.requestedAt >= ?13)
            AND (?14 is Null OR a.requestedAt <= ?14)
            """)
    Page<AccountRespDto> dynamicFilterAccount(AccountType accountType, BigDecimal minBalance, BigDecimal maxBalance, Boolean isActive,
                                              RequestStatus requestStatus, LocalDate createdFrom, LocalDate createdTill,
                                              LocalDate activatedFrom, LocalDate activatedTill, String ifscCode, Long customerId,
                                              Long accountId, LocalDate requestedFrom, LocalDate requestedTill, Pageable pageable);

    @Query("""
            Select a.id as accountId, a.accountType as accountType, a.balance as balance, a.isActive as isActive,
            a.requestStatus as requestStatus, c.id as customerId, c.name as customerName, e.id as employeeId,
            e.name as employeeName
            from Account a
            Join a.customer c
            Join a.employee e
            Where a.id = ?1
            """)
    AccountStatusRespDto getUpdatedAccountStatus(Long accountId);

    @Query("""
            Select a.id as accountId, a.accountType as accountType, a.balance as balance, a.isActive as isActive,
            a.requestStatus as requestStatus, a.requestReason as requestReason, a.requestedAt as requestedAt,
            a.createdAt as createdAt, a.activatedAt as activatedAt, b.branchName as branchName, b.ifscCode as ifscCode,
            c.id as customerId, c.name as customerName
            from Account a
            Join a.branch  b
            Join a.customer c
            Where a.id=?1
            """)
    AccountRespDto getAccountInfoById(Long accountId);

    @Query("""
        select a.requestStatus as requestStatus, count(a.id) as numberOfAccounts
        from Account a
        where a.branch.id = ?1
        group by a.requestStatus
        """)
    List<AccountRequestStatusStatDto> getAccountRequestStatusStats(Long branchId);
    @Query("""
        select b.branchName as branchName, count(a.id) as numberOfAccounts
        from Branch b
        left join Account a on a.branch.id = b.id
        group by b.branchName
        """)
    List<AccountBranchStatDto> getAccountBranchStats();
}
