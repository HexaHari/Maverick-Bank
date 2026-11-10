package com.bank.MaverickBank.repository;

import com.bank.MaverickBank.dto.response.BranchRespDto;
import com.bank.MaverickBank.model.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch,Long> {
    @Query("""
            Select b
            From Branch b
            Where b.ifscCode=?1
            """)
    Branch findBranchByIfscCode(String ifscCode);

    @Query("""
            Select b.id as branchId, b.branchName as branchName, b.ifscCode as ifscCode, b.addedAt as addedAt
            From Branch b
            Where (?1 is Null OR b.id = ?1)
            And (?2 is Null OR lower(b.branchName) like lower(concat('%', ?2, '%')))
            And (?3 is Null OR b.ifscCode like concat('%', ?3, '%'))
            And (?4 is Null OR b.addedAt >= ?4)
            And (?5 is Null OR b.addedAt <= ?5)
            """)
    Page<BranchRespDto> dynamicFilterBranch(Long branchId, String branchName, String ifscCode, LocalDate addedFrom,
                                            LocalDate addedTill, Pageable pageable);
}
