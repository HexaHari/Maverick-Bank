package com.bank.MaverickBank.contoller;

import com.bank.MaverickBank.dto.request.EmployeeReqDto;
import com.bank.MaverickBank.dto.response.EmployeeFilterDto;
import com.bank.MaverickBank.enums.JobTitle;
import com.bank.MaverickBank.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/api/employee/batch/add")
    public void addEmployeesInBatch(@Valid@RequestBody List<EmployeeReqDto> list){
        employeeService.addEmployeesInBatch(list);
    }

//    @PostMapping("/api/employee/add")
//    public void addEmployeesInBatch(@Valid@RequestBody EmployeeReqDto employeeReqDto){
//        employeeService.addEmployee(employeeReqDto);
//    }


    @GetMapping("/api/employee/filter")
    public EmployeeFilterDto dynamicFilterEmployee(@RequestParam(name = "name", required = false) String name,
                                                   @RequestParam(name = "contact", required = false) String contact,
                                                   @RequestParam(name = "jobTitle", required = false) JobTitle jobTitle,
                                                   @RequestParam(name = "ifscCode", required = false) String ifscCode,
                                                   @RequestParam(name = "isActive", required = false) Boolean isActive,
                                                   @RequestParam(name = "employeeId", required = false) Long employeeId,
                                                   @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                                   @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {
        return employeeService.dynamicFilterEmployee(name, contact, jobTitle, ifscCode, isActive, employeeId, page, size);
    }
}
