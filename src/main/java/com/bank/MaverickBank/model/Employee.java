package com.bank.MaverickBank.model;

import com.bank.MaverickBank.enums.JobTitle;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(unique = true,nullable = false)
    private String contact;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,name = "job_title")
    private JobTitle jobTitle;
    @ManyToOne
    private Branch branch;
    @OneToOne
    @JoinColumn(nullable = false)
    private User user;
}
