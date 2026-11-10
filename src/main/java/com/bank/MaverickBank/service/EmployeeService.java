package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.EmployeeReqDto;
import com.bank.MaverickBank.dto.response.EmployeeFilterDto;
import com.bank.MaverickBank.dto.response.EmployeeRespDto;
import com.bank.MaverickBank.dto.response.PaginationDto;
import com.bank.MaverickBank.enums.JobTitle;
import com.bank.MaverickBank.enums.Role;
import com.bank.MaverickBank.exception.ResourceNotFoundException;
import com.bank.MaverickBank.mapper.EmployeeMapper;
import com.bank.MaverickBank.model.Branch;
import com.bank.MaverickBank.model.Employee;
import com.bank.MaverickBank.model.User;
import com.bank.MaverickBank.repository.BranchRepository;
import com.bank.MaverickBank.repository.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmployeeService {

    private final BranchService branchService;
    private final UserService userService;
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;

    public void addEmployee(@Valid EmployeeReqDto employeeReqDto) {
        Employee employee = EmployeeMapper.mapDtoToModel(employeeReqDto);
        Branch branch = Optional.ofNullable(branchRepository.findBranchByIfscCode(employeeReqDto.ifscCode()))
                .orElseThrow(()-> new ResourceNotFoundException("Branch IFSC Code is Invalid"));
        //save and fetch the user object
        User user= userService.getUserObject(employeeReqDto.username(),
                employeeReqDto.password(),
                Role.EMPLOYEE);
        employee.setBranch(branch);
        employee.setUser(user);
        employeeRepository.save(employee);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addEmployeesInBatch(List<EmployeeReqDto> list) {
        log.info("Processing Multiple Employees at the same time");
        //Extract all unique IFSC Code from the batch
        List<String> ifscCodes = list.stream()
                .map(EmployeeReqDto::ifscCode)
                .distinct()
                .toList();
        //creating a map to get the Branch for appropriate IFSc code
        Map<String, Branch> branchMap = ifscCodes.stream()
                .map(branchService::getBranchByIfscCode)
                .collect(Collectors.toMap(Branch::getIfscCode, branch -> branch));

        List<Employee> employees=list.stream().map(employeeReqDto -> {
            //mapping primitive fields first
            Employee employee = EmployeeMapper.mapDtoToModel(employeeReqDto);
            // fetch and validate Branch
            Branch branch = Optional.ofNullable(branchMap.get(employeeReqDto.ifscCode()))
                    .orElseThrow(()-> new ResourceNotFoundException("Branch IFSC Code is Invalid"));
            //save and fetch the user object
            User user= userService.getUserObject(employeeReqDto.username(),
                                                 employeeReqDto.password(),
                                                 Role.EMPLOYEE);
            //set the foreign objects
            employee.setBranch(branch);
            employee.setUser(user);

            return employee;
        }).toList();
        employeeRepository.saveAll(employees);
        log.info("Finished Processing Multiple Employees at the same time");
    }

    public EmployeeFilterDto dynamicFilterEmployee(String name, String contact, JobTitle jobTitle,
                                                   String ifscCode, Boolean isActive, Long employeeId,
                                                   Integer page, Integer size) {
        log.info("Searching Employees through Dynamic Filter");
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeRespDto> pageDto = employeeRepository.dynamicFilterEmployee(name, contact, jobTitle,
                ifscCode, isActive, employeeId, pageable);
        return new EmployeeFilterDto(pageDto.getContent(),
                new PaginationDto(pageDto.getTotalPages(), pageDto.getTotalElements(), pageDto.getNumberOfElements()));
    }


}
