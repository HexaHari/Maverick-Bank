package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.EmployeeUpdateDto;
import com.bank.MaverickBank.dto.request.UserUpdateDto;
import com.bank.MaverickBank.dto.response.AccountBranchStatDto;
import com.bank.MaverickBank.dto.response.ChartDto;
import com.bank.MaverickBank.dto.response.EmployeeBranchStatDto;
import com.bank.MaverickBank.dto.response.UserFilterDto;
import com.bank.MaverickBank.dto.response.UserRespDto;
import com.bank.MaverickBank.enums.JobTitle;
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
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {
    @InjectMocks
    private AdminService adminService;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Principal principal;

    private Employee createEmployee(){
        Branch oldBranch = new Branch();
        oldBranch.setId(1L);
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("Old Name");
        employee.setContact("9000000001");
        employee.setJobTitle(JobTitle.CLERK);
        employee.setBranch(oldBranch);
        return employee;
    }

    private User createUser(Long id,String username,Role role){
        User user = new User(username,"oldEncodedPass",role);
        user.setId(id);
        return user;
    }


    @Test
    public void updateEmployeeTest(){
        Employee employee = createEmployee();
        Branch newBranch = new Branch();
        newBranch.setId(2L);
        User newUser = createUser(3L,"ravi01",Role.EMPLOYEE);
        EmployeeUpdateDto dto = new EmployeeUpdateDto("New Name","9876543210",JobTitle.MANAGER,2L,3L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByContactAndIdNot("9876543210",1L)).thenReturn(false);
        when(branchRepository.findById(2L)).thenReturn(Optional.of(newBranch));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newUser));
        when(employeeRepository.existsByUserIdAndIdNot(3L,1L)).thenReturn(false);

        adminService.updateEmployee(1L,dto);

        assertEquals("New Name",employee.getName());
        assertEquals("9876543210",employee.getContact());
        assertEquals(JobTitle.MANAGER,employee.getJobTitle());
        assertEquals(newBranch,employee.getBranch());
        assertEquals(newUser,employee.getUser());

        verify(employeeRepository,times(1)).findById(1L);
        verify(employeeRepository,times(1)).existsByContactAndIdNot("9876543210",1L);
        verify(branchRepository,times(1)).findById(2L);
        verify(userRepository,times(1)).findById(3L);
        verify(employeeRepository,times(1)).existsByUserIdAndIdNot(3L,1L);
        verify(employeeRepository,times(1)).save(employee);

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void updateEmployeeTest_OnlyName(){
        //the fields that are null in the dto must stay as they were
        Employee employee = createEmployee();
        EmployeeUpdateDto dto = new EmployeeUpdateDto("New Name",null,null,null,null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        adminService.updateEmployee(1L,dto);

        assertEquals("New Name",employee.getName());
        assertEquals("9000000001",employee.getContact());
        assertEquals(JobTitle.CLERK,employee.getJobTitle());
        assertEquals(1L,employee.getBranch().getId());
        assertNull(employee.getUser());

        verify(employeeRepository,times(1)).findById(1L);
        verify(employeeRepository,times(1)).save(employee);

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void updateEmployeeTest_InvalidEmployee(){
        EmployeeUpdateDto dto = new EmployeeUpdateDto("New Name",null,null,null,null);

        when(employeeRepository.findById(5L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->adminService.updateEmployee(5L,dto));
        assertEquals("Employee ID is invalid",exception.getMessage());

        verify(employeeRepository,times(1)).findById(5L);
        verify(employeeRepository,times(0)).save(any(Employee.class));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void updateEmployeeTest_ContactExists(){
        Employee employee = createEmployee();
        EmployeeUpdateDto dto = new EmployeeUpdateDto(null,"9876543210",null,null,null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByContactAndIdNot("9876543210",1L)).thenReturn(true);

        assertThrows(OutOfReachException.class,()->adminService.updateEmployee(1L,dto));

        verify(employeeRepository,times(1)).findById(1L);
        verify(employeeRepository,times(1)).existsByContactAndIdNot("9876543210",1L);
        verify(employeeRepository,times(0)).save(any(Employee.class));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void updateEmployeeTest_InvalidBranch(){
        Employee employee = createEmployee();
        EmployeeUpdateDto dto = new EmployeeUpdateDto(null,null,null,9L,null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(branchRepository.findById(9L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->adminService.updateEmployee(1L,dto));
        assertEquals("Branch ID is Invalid",exception.getMessage());

        verify(employeeRepository,times(1)).findById(1L);
        verify(branchRepository,times(1)).findById(9L);
        verify(employeeRepository,times(0)).save(any(Employee.class));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void updateEmployeeTest_UserNotEmployee(){
        Employee employee = createEmployee();
        User customerUser = createUser(3L,"monisha",Role.CUSTOMER);
        EmployeeUpdateDto dto = new EmployeeUpdateDto(null,null,null,null,3L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(3L)).thenReturn(Optional.of(customerUser));

        //only a user with EMPLOYEE role can be linked to an employee
        assertThrows(OutOfReachException.class,()->adminService.updateEmployee(1L,dto));

        verify(employeeRepository,times(1)).findById(1L);
        verify(userRepository,times(1)).findById(3L);
        verify(employeeRepository,times(0)).save(any(Employee.class));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void updateEmployeeTest_UserAlreadyLinked(){
        Employee employee = createEmployee();
        User employeeUser = createUser(3L,"ravi01",Role.EMPLOYEE);
        EmployeeUpdateDto dto = new EmployeeUpdateDto(null,null,null,null,3L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(3L)).thenReturn(Optional.of(employeeUser));
        when(employeeRepository.existsByUserIdAndIdNot(3L,1L)).thenReturn(true);

        assertThrows(OutOfReachException.class,()->adminService.updateEmployee(1L,dto));

        verify(employeeRepository,times(1)).findById(1L);
        verify(userRepository,times(1)).findById(3L);
        verify(employeeRepository,times(1)).existsByUserIdAndIdNot(3L,1L);
        verify(employeeRepository,times(0)).save(any(Employee.class));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void updateUserTest(){
        User user = createUser(2L,"ravi",Role.EMPLOYEE);
        UserUpdateDto dto = new UserUpdateDto("newPass",Role.CUSTOMER,false);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(principal.getName()).thenReturn("admin");
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        adminService.updateUser(2L,dto,principal);

        assertEquals("encodedNewPass",user.getPassword());
        assertEquals(Role.CUSTOMER,user.getRole());
        assertFalse(user.isActive());

        verify(userRepository,times(1)).findById(2L);
        verify(principal,times(1)).getName();
        verify(passwordEncoder,times(1)).encode("newPass");
        verify(userRepository,times(1)).save(user);

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void updateUserTest_Invalid(){
        UserUpdateDto dto = new UserUpdateDto("newPass",null,null);

        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,()->adminService.updateUser(9L,dto,principal));
        assertEquals("User not found",exception.getMessage());

        verify(userRepository,times(1)).findById(9L);
        verify(userRepository,times(0)).save(any(User.class));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void updateUserTest_AdminChangesOwnRole(){
        User admin = createUser(1L,"admin",Role.ADMIN);
        UserUpdateDto dto = new UserUpdateDto(null,Role.CUSTOMER,null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(principal.getName()).thenReturn("admin");

        assertThrows(OutOfReachException.class,()->adminService.updateUser(1L,dto,principal));
        assertEquals(Role.ADMIN,admin.getRole());

        verify(userRepository,times(1)).findById(1L);
        verify(principal,times(1)).getName();
        verify(userRepository,times(0)).save(any(User.class));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void updateUserTest_AdminDeactivatesSelf(){
        User admin = createUser(1L,"admin",Role.ADMIN);
        UserUpdateDto dto = new UserUpdateDto(null,null,false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(principal.getName()).thenReturn("admin");

        RuntimeException exception = assertThrows(RuntimeException.class,()->adminService.updateUser(1L,dto,principal));
        assertEquals("ADMIN active status cannot be set a false",exception.getMessage());
        assertTrue(admin.isActive());

        verify(userRepository,times(1)).findById(1L);
        verify(principal,times(1)).getName();
        verify(userRepository,times(0)).save(any(User.class));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void getEmployeeBranchStatsTest(){
        when(employeeRepository.getEmployeeBranchStats())
                .thenReturn(List.of(new EmployeeBranchStatDto("Chennai",3L),new EmployeeBranchStatDto("Madurai",1L)));

        ChartDto result = adminService.getEmployeeBranchStats();

        assertEquals(List.of("Chennai","Madurai"),result.labels());
        assertEquals(List.of(3L,1L),result.values());

        verify(employeeRepository,times(1)).getEmployeeBranchStats();

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void getEmployeeBranchStatsTest_NoBranches(){
        when(employeeRepository.getEmployeeBranchStats()).thenReturn(List.of());

        ChartDto result = adminService.getEmployeeBranchStats();

        assertTrue(result.labels().isEmpty());
        assertTrue(result.values().isEmpty());

        verify(employeeRepository,times(1)).getEmployeeBranchStats();

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void getAccountBranchStatsTest(){
        when(accountRepository.getAccountBranchStats())
                .thenReturn(List.of(new AccountBranchStatDto("Chennai",10L),new AccountBranchStatDto("Madurai",4L)));

        ChartDto result = adminService.getAccountBranchStats();

        assertEquals(List.of("Chennai","Madurai"),result.labels());
        assertEquals(List.of(10L,4L),result.values());

        verify(accountRepository,times(1)).getAccountBranchStats();

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void deactivateUserTest(){
        User user = createUser(2L,"ravi",Role.EMPLOYEE);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(principal.getName()).thenReturn("admin");

        adminService.deactivateUser(2L,principal);

        assertFalse(user.isActive());

        verify(userRepository,times(1)).findById(2L);
        verify(principal,times(1)).getName();
        verify(userRepository,times(1)).save(user);

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void deactivateUserTest_InvalidUser(){
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->adminService.deactivateUser(9L,principal));
        assertEquals("User ID is invalid",exception.getMessage());

        verify(userRepository,times(1)).findById(9L);
        verify(userRepository,times(0)).save(any(User.class));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void deactivateUserTest_AdminDeactivatesSelf(){
        User admin = createUser(1L,"admin",Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(principal.getName()).thenReturn("admin");

        OutOfReachException exception = assertThrows(OutOfReachException.class,
                ()->adminService.deactivateUser(1L,principal));
        assertEquals("ADMIN ID cannot be deactivated",exception.getMessage());
        assertTrue(admin.isActive());

        verify(userRepository,times(1)).findById(1L);
        verify(principal,times(1)).getName();
        verify(userRepository,times(0)).save(any(User.class));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void dynamicFilterUserTest(){
        UserRespDto dto1 = new UserRespDto(2L,"ravi",Role.EMPLOYEE,true,LocalDate.of(2026,10,21),LocalDate.of(2026,10,21));
        UserRespDto dto2 = new UserRespDto(3L,"raj",Role.EMPLOYEE,true,LocalDate.of(2026,10,22),LocalDate.of(2026,10,22));
        LocalDate createdFrom = LocalDate.of(2026,10,1);
        LocalDate createdTill = LocalDate.of(2026,10,31);

        when(userRepository.dynamicFilterUser(null,"ra",Role.EMPLOYEE,true,createdFrom,createdTill,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(dto1,dto2),PageRequest.of(0,5),2));

        UserFilterDto result = adminService.dynamicFilterUser(null,"ra",Role.EMPLOYEE,true,createdFrom,createdTill,0,5);

        assertEquals(2,result.list().size());
        assertEquals("ravi",result.list().get(0).username());
        assertEquals(1,result.paginationInfo().totalPages());
        assertEquals(2L,result.paginationInfo().totalRecords());
        assertEquals(2,result.paginationInfo().numRecordsInPage());

        verify(userRepository,times(1))
                .dynamicFilterUser(null,"ra",Role.EMPLOYEE,true,createdFrom,createdTill,PageRequest.of(0,5));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void dynamicFilterUserTest_Invalid(){
        //page number can not be negative, so the repository is never reached
        assertThrows(IllegalArgumentException.class,
                ()->adminService.dynamicFilterUser(null,null,null,null,null,null,-1,5));

        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }
}
