package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.EmployeeReqDto;
import com.bank.MaverickBank.dto.response.EmployeeFilterDto;
import com.bank.MaverickBank.dto.response.EmployeeRespDto;
import com.bank.MaverickBank.enums.JobTitle;
import com.bank.MaverickBank.enums.Role;
import com.bank.MaverickBank.exception.ResourceNotFoundException;
import com.bank.MaverickBank.model.Branch;
import com.bank.MaverickBank.model.Employee;
import com.bank.MaverickBank.model.User;
import com.bank.MaverickBank.repository.BranchRepository;
import com.bank.MaverickBank.repository.EmployeeRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    @InjectMocks
    private EmployeeService employeeService;
    @Mock
    private BranchService branchService;
    @Mock
    private UserService userService;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private BranchRepository branchRepository;

    @Test
    public void addEmployeeTest(){
        EmployeeReqDto dto = new EmployeeReqDto("Ravi","9876543210",JobTitle.CLERK,"MAVB0000300","ravi01","pass123");
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setIfscCode("MAVB0000300");
        User user = new User("ravi01","encodedPass",Role.EMPLOYEE);
        user.setId(5L);

        when(branchRepository.findBranchByIfscCode("MAVB0000300")).thenReturn(branch);
        when(userService.getUserObject("ravi01","pass123",Role.EMPLOYEE)).thenReturn(user);

        employeeService.addEmployee(dto);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(branchRepository,times(1)).findBranchByIfscCode("MAVB0000300");
        verify(userService,times(1)).getUserObject("ravi01","pass123",Role.EMPLOYEE);
        verify(employeeRepository,times(1)).save(captor.capture());

        //the saved employee must have the branch and the user attached
        Employee saved = captor.getValue();
        assertEquals("Ravi",saved.getName());
        assertEquals("9876543210",saved.getContact());
        assertEquals(JobTitle.CLERK,saved.getJobTitle());
        assertEquals(branch,saved.getBranch());
        assertEquals(user,saved.getUser());

        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(branchService);
    }

    @Test
    public void addEmployeeTest_Invalid(){
        EmployeeReqDto dto = new EmployeeReqDto("Ravi","9876543210",JobTitle.CLERK,"MAVB0009999","ravi01","pass123");

        when(branchRepository.findBranchByIfscCode("MAVB0009999")).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->employeeService.addEmployee(dto));
        assertEquals("Branch IFSC Code is Invalid",exception.getMessage());

        //no login should be created for an employee that has no valid branch
        verify(branchRepository,times(1)).findBranchByIfscCode("MAVB0009999");
        verify(userService,times(0)).getUserObject("ravi01","pass123",Role.EMPLOYEE);
        verify(employeeRepository,times(0)).save(any(Employee.class));

        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(employeeRepository);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addEmployeesInBatchTest(){
        EmployeeReqDto dto1 = new EmployeeReqDto("Ravi","9876543210",JobTitle.CLERK,"MAVB0000300","ravi01","pass1");
        EmployeeReqDto dto2 = new EmployeeReqDto("Meena","9876543211",JobTitle.MANAGER,"MAVB0000300","meena02","pass2");
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setIfscCode("MAVB0000300");
        User user1 = new User("ravi01","encoded1",Role.EMPLOYEE);
        User user2 = new User("meena02","encoded2",Role.EMPLOYEE);

        when(branchService.getBranchByIfscCode("MAVB0000300")).thenReturn(branch);
        when(userService.getUserObject("ravi01","pass1",Role.EMPLOYEE)).thenReturn(user1);
        when(userService.getUserObject("meena02","pass2",Role.EMPLOYEE)).thenReturn(user2);

        employeeService.addEmployeesInBatch(List.of(dto1,dto2));

        ArgumentCaptor<List<Employee>> captor = ArgumentCaptor.forClass(List.class);
        //both employees are in the same branch, so the branch is fetched only once
        verify(branchService,times(1)).getBranchByIfscCode("MAVB0000300");
        verify(userService,times(1)).getUserObject("ravi01","pass1",Role.EMPLOYEE);
        verify(userService,times(1)).getUserObject("meena02","pass2",Role.EMPLOYEE);
        verify(employeeRepository,times(1)).saveAll(captor.capture());

        List<Employee> saved = captor.getValue();
        assertEquals(2,saved.size());
        assertEquals("Ravi",saved.get(0).getName());
        assertEquals(user1,saved.get(0).getUser());
        assertEquals(JobTitle.MANAGER,saved.get(1).getJobTitle());
        assertEquals(branch,saved.get(1).getBranch());

        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(branchRepository);
    }

    @Test
    public void addEmployeesInBatchTest_Invalid(){
        EmployeeReqDto dto = new EmployeeReqDto("Ravi","9876543210",JobTitle.CLERK,"MAVB0009999","ravi01","pass1");

        when(branchService.getBranchByIfscCode("MAVB0009999")).thenReturn(null);

        //nothing is saved when one of the IFSC codes is not a real branch
        assertThrows(RuntimeException.class,()->employeeService.addEmployeesInBatch(List.of(dto)));

        verify(branchService,times(1)).getBranchByIfscCode("MAVB0009999");
        verify(employeeRepository,times(0)).saveAll(anyList());

        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(employeeRepository);
    }

    @Test
    public void dynamicFilterEmployeeTest(){
        EmployeeRespDto dto1 = new EmployeeRespDto(1L,"Ravi","9876543210",JobTitle.CLERK,"Chennai Main","MAVB0000300","ravi01",true);
        EmployeeRespDto dto2 = new EmployeeRespDto(2L,"Raj","9876543212",JobTitle.CLERK,"Chennai Main","MAVB0000300","raj03",true);

        when(employeeRepository.dynamicFilterEmployee("Ra",null,JobTitle.CLERK,"MAVB0000300",true,null,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(dto1,dto2),PageRequest.of(0,5),2));

        EmployeeFilterDto result = employeeService.dynamicFilterEmployee("Ra",null,JobTitle.CLERK,"MAVB0000300",true,null,0,5);

        assertEquals(2,result.employeeList().size());
        assertEquals("Ravi",result.employeeList().get(0).name());
        assertEquals(1,result.paginationInfo().totalPages());
        assertEquals(2L,result.paginationInfo().totalRecords());
        assertEquals(2,result.paginationInfo().numRecordsInPage());

        verify(employeeRepository,times(1))
                .dynamicFilterEmployee("Ra",null,JobTitle.CLERK,"MAVB0000300",true,null,PageRequest.of(0,5));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(branchRepository);
    }

    @Test
    public void dynamicFilterEmployeeTest_Invalid(){
        //page size can not be zero, so the repository is never reached
        assertThrows(IllegalArgumentException.class,
                ()->employeeService.dynamicFilterEmployee(null,null,null,null,null,null,0,0));

        verifyNoMoreInteractions(employeeRepository);
    }
}
