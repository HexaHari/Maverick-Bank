package com.bank.MaverickBank.repository;

import com.bank.MaverickBank.dto.response.CustomerFilterRespDto;
import com.bank.MaverickBank.enums.Gender;
import com.bank.MaverickBank.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {

    @Query("""
            Select c
            From Customer c
            Join c.user u
            Where u.username=?1
            """)
    Customer getCustomerByUsername(String customerUsername);

    @Query("""
        Select c.id as customerId, c.name as name, c.dateOfBirth as dateOfBirth, c.gender as gender,
        c.contact as contact, c.address as address, c.aadharNumber as aadharNumber, c.panNumber as panNumber,
        c.picture as picture, c.identificationDoc as identificationDoc
        From Customer c
        Where (?1 is Null OR c.id = ?1)
        AND (?2 is Null OR LOWER(c.name) LIKE LOWER(CONCAT('%', ?2, '%')))
        AND (?3 is Null OR c.gender = ?3)
        AND (?4 is Null OR c.contact LIKE CONCAT('%', ?4, '%'))
        AND (?5 is Null OR c.aadharNumber LIKE CONCAT('%', ?5, '%'))
        AND (?6 is Null OR c.panNumber LIKE CONCAT('%', ?6, '%'))
        AND (?7 is Null OR LOWER(c.address) LIKE LOWER(CONCAT('%', ?7, '%')))
        AND (?8 is Null OR c.dateOfBirth >= ?8)
        AND (?9 is Null OR c.dateOfBirth <= ?9)
        """)
    Page<CustomerFilterRespDto> dynamicFilterCustomer(Long customerId, String name, Gender gender, String contact,
                                                      String aadharNumber, String panNumber, String address,
                                                      LocalDate dobFrom, LocalDate dobTill, Pageable pageable);
}
