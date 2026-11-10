package com.bank.MaverickBank.repository;

import com.bank.MaverickBank.dto.response.EmployeeBranchStatDto;
import com.bank.MaverickBank.dto.response.EmployeeRespDto;
import com.bank.MaverickBank.enums.JobTitle;
import com.bank.MaverickBank.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {

    @Query("""
            Select e
            from Employee e
            Join e.user u
            where u.username = ?1
            """)
    Employee getEmployeeByUsername(String employeeUsername);

    @Query("""
        Select e.id, e.name, e.contact, e.jobTitle, b.branchName, b.ifscCode, u.username, u.isActive
        from Employee e
        Join e.branch b
        Join e.user u
        Where (?1 is Null OR lower(e.name) like lower(concat('%', ?1, '%')))
        AND (?2 is Null OR e.contact like concat('%', ?2, '%'))
        AND (?3 is Null OR e.jobTitle = ?3)
        AND (?4 is Null OR b.ifscCode like concat('%', ?4, '%'))
        AND (?5 is Null OR u.isActive = ?5)
        AND (?6 is Null OR e.id = ?6)
        """)
    Page<EmployeeRespDto> dynamicFilterEmployee(String name, String contact, JobTitle jobTitle,
                                                String ifscCode, Boolean isActive, Long employeeId,
                                                Pageable pageable);

    Optional<Employee> findByUserUsername(String username);

    boolean existsByContact(String contact);
    boolean existsByContactAndIdNot(String contact, Long id);
    boolean existsByUserIdAndIdNot(Long userId, Long id);

    @Query("""
        select b.branchName as branchName, count(e.id) as numberOfEmployees
        from Branch b
        left join Employee e on e.branch.id = b.id
        group by b.branchName
        """)
    List<EmployeeBranchStatDto> getEmployeeBranchStats();
}
