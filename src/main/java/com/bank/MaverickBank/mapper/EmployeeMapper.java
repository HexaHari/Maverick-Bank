package com.bank.MaverickBank.mapper;

import com.bank.MaverickBank.dto.request.EmployeeReqDto;
import com.bank.MaverickBank.model.Branch;
import com.bank.MaverickBank.model.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {
    public static Employee mapDtoToModel(EmployeeReqDto employeeReqDto){
        Employee employee = new Employee();
        employee.setName(employeeReqDto.name());
        employee.setContact(employeeReqDto.contact());
        employee.setJobTitle(employeeReqDto.jobTitle());
        return employee;
    }
}
