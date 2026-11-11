package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.EmployeeUpdateDto;
import com.bank.MaverickBank.dto.request.UserUpdateDto;
import com.bank.MaverickBank.dto.response.*;
import com.bank.MaverickBank.enums.Role;
import com.bank.MaverickBank.exception.OutOfReachException;
import com.bank.MaverickBank.exception.ResourceNotFoundException;
import com.bank.MaverickBank.model.Branch;
import com.bank.MaverickBank.model.Employee;
import com.bank.MaverickBank.model.User;
import com.bank.MaverickBank.repository.AccountRepository;
import com.bank.MaverickBank.repository.BranchRepository;
import com.bank.MaverickBank.repository.EmployeeRepository;
import com.bank.MaverickBank.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class AdminService {
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void updateEmployee(Long employeeId, EmployeeUpdateDto dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee ID is invalid"));

        if (dto.name() != null) {
            employee.setName(dto.name());
        }
        if (dto.contact() != null) {
            if (employeeRepository.existsByContactAndIdNot(dto.contact(), employeeId)) {
                throw new OutOfReachException("Contact already exists and cannot be assigned");
            }
            employee.setContact(dto.contact());
        }
        if (dto.jobTitle() != null) {
            employee.setJobTitle(dto.jobTitle());
        }
        if (dto.branchId() != null) {
            Branch branch = branchRepository.findById(dto.branchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch ID is Invalid"));
            employee.setBranch(branch);
        }
        if (dto.userId() != null) {
            User user = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("UserId is Invalid"));
            if (user.getRole() != Role.EMPLOYEE) {
                throw new OutOfReachException("Selected user is not an employee account");
            }
            if (employeeRepository.existsByUserIdAndIdNot(dto.userId(), employeeId)) {
                throw new OutOfReachException("User is already linked to another employee");
            }
            employee.setUser(user);
        }
        employeeRepository.save(employee);
    }

    public void updateUser(Long userId, UserUpdateDto dto, Principal principal) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Id is invalid"));
        boolean isSelf = user.getUsername().equals(principal.getName());

        if (dto.password() != null) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
        if (dto.role() != null) {
            if (isSelf) {
                throw new OutOfReachException("ADMIN role cannot be changed");
            }
            user.setRole(dto.role());
        }
        if (dto.isActive() != null) {
            if (isSelf) {
                throw new OutOfReachException("ADMIN active status cannot be changed");
            }
            user.setActive(dto.isActive());
        }
        userRepository.save(user);
    }

    public ChartDto getEmployeeBranchStats() {
        List<EmployeeBranchStatDto> list = employeeRepository.getEmployeeBranchStats();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        list.forEach(e -> {
            labels.add(e.branchName());
            values.add(e.numberOfEmployees());
        });
        return new ChartDto(labels, values);
    }

    public ChartDto getAccountBranchStats() {
        List<AccountBranchStatDto> list = accountRepository.getAccountBranchStats();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        list.forEach(e -> {
            labels.add(e.branchName());
            values.add(e.numberOfAccounts());
        });
        return new ChartDto(labels, values);
    }

    public void deactivateUser(Long userId, Principal principal) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User ID is invalid"));
        if (user.getUsername().equals(principal.getName())) {
            throw new OutOfReachException("ADMIN ID cannot be deactivated");
        }
        user.setActive(false);
        userRepository.save(user);
    }

    // AdminService
    public UserFilterDto dynamicFilterUser(Long userId, String username, Role role, Boolean isActive,
                                           LocalDate createdFrom, LocalDate createdTill,
                                           Integer page, Integer size) {
        log.info("Searching Users through Dynamic Filter");
        Pageable pageable = PageRequest.of(page, size);
        Page<UserRespDto> pageDto = userRepository.dynamicFilterUser(userId, username, role, isActive,
                createdFrom, createdTill, pageable);
        return new UserFilterDto(pageDto.getContent(),
                new PaginationDto(pageDto.getTotalPages(), pageDto.getTotalElements(), pageDto.getNumberOfElements()));
    }
}
