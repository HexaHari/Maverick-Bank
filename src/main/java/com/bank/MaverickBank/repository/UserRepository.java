package com.bank.MaverickBank.repository;

import com.bank.MaverickBank.dto.response.DetailsDto;
import com.bank.MaverickBank.dto.response.UserRespDto;
import com.bank.MaverickBank.enums.Role;
import com.bank.MaverickBank.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    @Query("""
            select u
            from User u
            where u.username=?1
            """)
    User loadUserByUsername(String username);

    @Query("""
            select u.username, u.isActive, u.role
            from User u
            where u.username=?1
            """)
    DetailsDto getUserDetails(String loggedInUsername);

    boolean existsByUsername(String username);

    // UserRepository
    @Query("""
        Select u.id, u.username, u.role, u.isActive, u.createAt, u.updatedAt
        from User u
        Where (?1 is Null OR u.id = ?1)
        AND (?2 is Null OR lower(u.username) like lower(concat('%', ?2, '%')))
        AND (?3 is Null OR u.role = ?3)
        AND (?4 is Null OR u.isActive = ?4)
        AND (?5 is Null OR u.createAt >= ?5)
        AND (?6 is Null OR u.createAt <= ?6)
        """)
    Page<UserRespDto> dynamicFilterUser(Long userId, String username, Role role, Boolean isActive,
                                        LocalDate createdFrom, LocalDate createdTill, Pageable pageable);
}
