package com.bank.MaverickBank.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true,nullable = false,name = "branch_name")
    private String branchName;
    @Column(unique = true,nullable = false,name = "ifsc_code")
    private String ifscCode;
    @CreationTimestamp
    @Column(name = "added_at")
    private LocalDate addedAt;
}
