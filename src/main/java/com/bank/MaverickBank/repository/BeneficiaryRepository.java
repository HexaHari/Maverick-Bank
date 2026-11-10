package com.bank.MaverickBank.repository;

import com.bank.MaverickBank.dto.response.BeneficiaryFilterRespDto;
import com.bank.MaverickBank.model.Beneficiary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary,Long> {
    @Query("""
            Select b
            from Beneficiary b
            Join b.beneficiaryAccount a
            Join b.customer c
            Where a.id =?1 And c.id = ?2
            """)
    Beneficiary getByAccountAndCustomerId(Long beneficiaryAccountId, Long customerId);

    @Query("""
            Select b.id as beneficiaryId, ba.id as beneficiaryAccountId, b.beneficiaryName as beneficiaryName,
            bc.name as beneficiaryCustomerName, bb.ifscCode as beneficiaryIfscCode, bb.branchName as beneficiaryBranchName,
            c.id as customerId, c.name as customerName, b.addedAt as addedAt
            from Beneficiary b
            Left Join b.beneficiaryAccount ba
            Left Join b.customer c
            Left Join ba.customer bc
            Left Join ba.branch bb
            Where (?1 is Null OR b.id = ?1)
            And (?2 is Null OR ba.id = ?2)
            And (?3 is Null OR c.id = ?3)
            And (?4 is Null OR b.beneficiaryName LIKE CONCAT('%', ?4, '%'))
            And (?5 is Null OR bb.ifscCode LIKE CONCAT('%', ?5, '%'))
            And (?6 is Null OR b.addedAt >= ?6)
            And (?7 is Null OR b.addedAt <= ?7)
            """)
    Page<BeneficiaryFilterRespDto> dynamicFilterBeneficiary(Long beneficiaryId, Long beneficiaryAccountId, Long customerId,
                                                            String beneficiaryName, String beneficiaryIfscCode, LocalDate addedFrom,
                                                            LocalDate addedTill, Pageable pageable);
}
