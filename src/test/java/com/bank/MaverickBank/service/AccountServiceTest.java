package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.AccountCloseReqDto;
import com.bank.MaverickBank.dto.request.AccountOpenReqDto;
import com.bank.MaverickBank.dto.response.AccountFilterDto;
import com.bank.MaverickBank.dto.response.AccountRequestStatusStatDto;
import com.bank.MaverickBank.dto.response.AccountRespDto;
import com.bank.MaverickBank.dto.response.AccountStatusRespDto;
import com.bank.MaverickBank.dto.response.ChartDto;
import com.bank.MaverickBank.enums.AccountType;
import com.bank.MaverickBank.enums.RequestStatus;
import com.bank.MaverickBank.exception.OutOfReachException;
import com.bank.MaverickBank.exception.OwnershipException;
import com.bank.MaverickBank.exception.ResourceNotFoundException;
import com.bank.MaverickBank.exception.UnchangedStateException;
import com.bank.MaverickBank.model.Account;
import com.bank.MaverickBank.model.Branch;
import com.bank.MaverickBank.model.Customer;
import com.bank.MaverickBank.model.Employee;
import com.bank.MaverickBank.repository.AccountRepository;
import com.bank.MaverickBank.repository.CustomerRepository;
import com.bank.MaverickBank.repository.EmployeeRepository;
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

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @InjectMocks
    private AccountService accountService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private BranchService branchService;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Principal principal;

    private Branch createBranch(Long id){
        Branch branch = new Branch();
        branch.setId(id);
        branch.setBranchName("Branch" + id);
        branch.setIfscCode("MAVB000030" + id);
        return branch;
    }

    private Customer createCustomer(Long id){
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName("Customer" + id);
        return customer;
    }

    private Employee createEmployee(Long id,Branch branch){
        Employee employee = new Employee();
        employee.setId(id);
        employee.setName("Employee" + id);
        employee.setBranch(branch);
        return employee;
    }

    private Account createAccount(Long id,boolean active,Customer customer,Branch branch){
        Account account = new Account();
        account.setId(id);
        account.setAccountType(AccountType.SAVINGS);
        account.setActive(active);
        account.setRequestStatus(RequestStatus.NONE);
        account.setCustomer(customer);
        account.setBranch(branch);
        return account;
    }


    @Test
    public void addAccountTest(){
        AccountOpenReqDto dto = new AccountOpenReqDto(AccountType.SAVINGS,"Salary account","MAVB0000301","1234");
        Customer customer = createCustomer(7L);
        Branch branch = createBranch(1L);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(branchService.getBranchByIfscCode("MAVB0000301")).thenReturn(branch);
        when(passwordEncoder.encode("1234")).thenReturn("encodedPin");

        accountService.addAccount(dto,"monisha");

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(branchService,times(1)).getBranchByIfscCode("MAVB0000301");
        verify(passwordEncoder,times(1)).encode("1234");
        verify(accountRepository,times(1)).save(any(Account.class));

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void addAccountTest_Invalid(){
        AccountOpenReqDto dto = new AccountOpenReqDto(AccountType.SAVINGS,"Salary account","MAVB0009999","1234");
        Customer customer = createCustomer(7L);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(branchService.getBranchByIfscCode("MAVB0009999")).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->accountService.addAccount(dto,"monisha"));
        assertEquals("Branch IFSC Code is invalid",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(branchService,times(1)).getBranchByIfscCode("MAVB0009999");
        verify(passwordEncoder,times(0)).encode("1234");
        verify(accountRepository,times(0)).save(any(Account.class));

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void dynamicFilterAccountTest(){
        AccountRespDto dto1 = new AccountRespDto(11L,AccountType.SAVINGS,new BigDecimal("5000.00"),true,RequestStatus.NONE,"None",
                LocalDate.of(2026,10,1),LocalDate.of(2026,10,1),LocalDate.of(2026,10,2),"Branch1","MAVB0000301",7L,"Customer7");
        AccountRespDto dto2 = new AccountRespDto(12L,AccountType.SAVINGS,new BigDecimal("900.00"),true,RequestStatus.NONE,"None",
                LocalDate.of(2026,10,3),LocalDate.of(2026,10,3),LocalDate.of(2026,10,4),"Branch1","MAVB0000301",8L,"Customer8");
        BigDecimal minBalance = new BigDecimal("100");

        when(accountRepository.dynamicFilterAccount(AccountType.SAVINGS,minBalance,null,true,null,null,null,null,null,
                "MAVB0000301",null,null,null,null,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(dto1,dto2),PageRequest.of(0,5),2));

        AccountFilterDto result = accountService.dynamicFilterAccount(AccountType.SAVINGS,minBalance,null,true,null,null,null,
                null,null,"MAVB0000301",null,null,null,null,0,5);

        assertEquals(2,result.list().size());
        assertEquals(11L,result.list().get(0).accountId());
        assertEquals(1,result.paginationInfo().totalPages());
        assertEquals(2L,result.paginationInfo().totalRecords());
        assertEquals(2,result.paginationInfo().numRecordsInPage());

        verify(accountRepository,times(1)).dynamicFilterAccount(AccountType.SAVINGS,minBalance,null,true,null,null,null,null,null,
                "MAVB0000301",null,null,null,null,PageRequest.of(0,5));

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void dynamicFilterAccountTest_Invalid(){
        //page number can not be negative, so the repository is never reached
        assertThrows(IllegalArgumentException.class,
                ()->accountService.dynamicFilterAccount(null,null,null,null,null,null,null,null,null,null,null,null,null,null,-1,5));

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void dynamicFilterCustomerAccountTest(){
        Customer customer = createCustomer(7L);
        AccountRespDto dto1 = new AccountRespDto(11L,AccountType.SAVINGS,new BigDecimal("5000.00"),true,RequestStatus.NONE,"None",
                LocalDate.of(2026,10,1),LocalDate.of(2026,10,1),LocalDate.of(2026,10,2),"Branch1","MAVB0000301",7L,"Customer7");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        //the customer id comes from the logged in customer, so a customer can only see own accounts
        when(accountRepository.dynamicFilterAccount(null,null,null,true,null,null,null,null,null,
                null,7L,null,null,null,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(dto1),PageRequest.of(0,5),1));

        AccountFilterDto result = accountService.dynamicFilterCustomerAccount(null,null,null,true,null,null,null,
                null,null,null,null,null,null,0,5,"monisha");

        assertEquals(1,result.list().size());
        assertEquals(7L,result.list().get(0).customerId());
        assertEquals(1,result.paginationInfo().totalPages());
        assertEquals(1L,result.paginationInfo().totalRecords());
        assertEquals(1,result.paginationInfo().numRecordsInPage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).dynamicFilterAccount(null,null,null,true,null,null,null,null,null,
                null,7L,null,null,null,PageRequest.of(0,5));

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void handleAccountTest(){
        //employee approves a new account of the same branch
        Branch branch = createBranch(1L);
        Employee employee = createEmployee(3L,branch);
        Account account = createAccount(11L,false,createCustomer(7L),branch);
        account.setRequestStatus(RequestStatus.OPEN);
        account.setRequestReason("Salary account");
        AccountStatusRespDto statusDto = new AccountStatusRespDto(11L,AccountType.SAVINGS,BigDecimal.ZERO,true,
                RequestStatus.NONE,7L,"Customer7",3L,"Employee3");

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(employee);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(accountRepository.getUpdatedAccountStatus(11L)).thenReturn(statusDto);

        AccountStatusRespDto result = accountService.handleAccount(11L,true,"clara");

        assertEquals(statusDto,result);
        assertTrue(account.isActive());
        assertEquals(LocalDate.now(),account.getActivatedAt());
        assertEquals(RequestStatus.NONE,account.getRequestStatus());
        assertEquals("None",account.getRequestReason());
        assertEquals(employee,account.getEmployee());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(1)).save(account);
        verify(accountRepository,times(1)).getUpdatedAccountStatus(11L);

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void handleAccountTest_Deactivate(){
        //employee closes an active account that the customer asked to close
        Branch branch = createBranch(1L);
        Employee employee = createEmployee(3L,branch);
        Account account = createAccount(11L,true,createCustomer(7L),branch);
        account.setRequestStatus(RequestStatus.CLOSE);
        account.setRequestReason("Not needed");
        AccountStatusRespDto statusDto = new AccountStatusRespDto(11L,AccountType.SAVINGS,BigDecimal.ZERO,false,
                RequestStatus.NONE,7L,"Customer7",3L,"Employee3");

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(employee);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(accountRepository.getUpdatedAccountStatus(11L)).thenReturn(statusDto);

        AccountStatusRespDto result = accountService.handleAccount(11L,false,"clara");

        assertEquals(statusDto,result);
        assertFalse(account.isActive());
        assertNull(account.getActivatedAt());
        assertEquals(RequestStatus.NONE,account.getRequestStatus());
        assertEquals(employee,account.getEmployee());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(1)).save(account);
        verify(accountRepository,times(1)).getUpdatedAccountStatus(11L);

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void handleAccountTest_InvalidAccount(){
        Employee employee = createEmployee(3L,createBranch(1L));

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(employee);
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->accountService.handleAccount(99L,true,"clara"));
        assertEquals("Account Id is Invalid",exception.getMessage());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(accountRepository,times(1)).findById(99L);
        verify(accountRepository,times(0)).save(any(Account.class));

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void handleAccountTest_DifferentBranch(){
        Employee employee = createEmployee(3L,createBranch(1L));
        Account account = createAccount(11L,false,createCustomer(7L),createBranch(2L));

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(employee);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        OutOfReachException exception = assertThrows(OutOfReachException.class,
                ()->accountService.handleAccount(11L,true,"clara"));
        assertEquals("Account and Employee are not from the same Branch",exception.getMessage());
        assertFalse(account.isActive());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(0)).save(any(Account.class));

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void handleAccountTest_UnchangedState(){
        //activating an account that is already active changes nothing
        Branch branch = createBranch(1L);
        Employee employee = createEmployee(3L,branch);
        Account account = createAccount(11L,true,createCustomer(7L),branch);

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(employee);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        UnchangedStateException exception = assertThrows(UnchangedStateException.class,
                ()->accountService.handleAccount(11L,true,"clara"));
        assertEquals("The Request doesn't change Account state",exception.getMessage());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(0)).save(any(Account.class));

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void closeAccountTest(){
        Customer customer = createCustomer(7L);
        Account account = createAccount(11L,true,customer,createBranch(1L));
        AccountCloseReqDto dto = new AccountCloseReqDto(11L,"Moving to another bank");
        AccountRespDto respDto = new AccountRespDto(11L,AccountType.SAVINGS,new BigDecimal("5000.00"),true,RequestStatus.CLOSE,
                "Moving to another bank",LocalDate.now(),LocalDate.of(2026,10,1),LocalDate.of(2026,10,2),"Branch1","MAVB0000301",7L,"Customer7");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(accountRepository.getAccountInfoById(11L)).thenReturn(respDto);

        AccountRespDto result = accountService.closeAccount(dto,"monisha");

        assertEquals(respDto,result);
        assertEquals(RequestStatus.CLOSE,account.getRequestStatus());
        assertEquals("Moving to another bank",account.getRequestReason());
        assertEquals(LocalDate.now(),account.getRequestedAt());
        //the account stays active until the employee closes it
        assertTrue(account.isActive());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(1)).save(account);
        verify(accountRepository,times(1)).getAccountInfoById(11L);

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void closeAccountTest_InvalidAccount(){
        Customer customer = createCustomer(7L);
        AccountCloseReqDto dto = new AccountCloseReqDto(99L,"Not needed");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->accountService.closeAccount(dto,"monisha"));
        assertEquals("Account ID is Invalid",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(99L);
        verify(accountRepository,times(0)).save(any(Account.class));

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void closeAccountTest_InactiveAccount(){
        Customer customer = createCustomer(7L);
        Account account = createAccount(11L,false,customer,createBranch(1L));
        AccountCloseReqDto dto = new AccountCloseReqDto(11L,"Not needed");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        OutOfReachException exception = assertThrows(OutOfReachException.class,
                ()->accountService.closeAccount(dto,"monisha"));
        assertEquals("Account is not accessible as it is Inactive",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(0)).save(any(Account.class));

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void closeAccountTest_NotOwner(){
        Customer loggedInCustomer = createCustomer(7L);
        Account account = createAccount(11L,true,createCustomer(8L),createBranch(1L));
        AccountCloseReqDto dto = new AccountCloseReqDto(11L,"Not needed");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(loggedInCustomer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        OwnershipException exception = assertThrows(OwnershipException.class,
                ()->accountService.closeAccount(dto,"monisha"));
        assertEquals("The Account does not belong to Customer",exception.getMessage());
        assertEquals(RequestStatus.NONE,account.getRequestStatus());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(0)).save(any(Account.class));

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void closeAccountTest_AlreadyRequested(){
        Customer customer = createCustomer(7L);
        Account account = createAccount(11L,true,customer,createBranch(1L));
        account.setRequestStatus(RequestStatus.CLOSE);
        AccountCloseReqDto dto = new AccountCloseReqDto(11L,"Not needed");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        UnchangedStateException exception = assertThrows(UnchangedStateException.class,
                ()->accountService.closeAccount(dto,"monisha"));
        assertEquals("This Account has already requested Close",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(0)).save(any(Account.class));

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void getAccountRequestStatusStatsTest(){
        Employee employee = createEmployee(3L,createBranch(1L));

        when(principal.getName()).thenReturn("clara");
        when(employeeRepository.findByUserUsername("clara")).thenReturn(Optional.of(employee));
        when(accountRepository.getAccountRequestStatusStats(1L))
                .thenReturn(List.of(new AccountRequestStatusStatDto(RequestStatus.OPEN,4L),
                                    new AccountRequestStatusStatDto(RequestStatus.CLOSE,1L),
                                    new AccountRequestStatusStatDto(RequestStatus.NONE,20L)));

        ChartDto result = accountService.getAccountRequestStatusStats(principal);

        assertEquals(List.of("OPEN","CLOSE","NONE"),result.labels());
        assertEquals(List.of(4L,1L,20L),result.values());

        verify(principal,times(1)).getName();
        verify(employeeRepository,times(1)).findByUserUsername("clara");
        verify(accountRepository,times(1)).getAccountRequestStatusStats(1L);

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }

    @Test
    public void getAccountRequestStatusStatsTest_Invalid(){
        when(principal.getName()).thenReturn("nobody");
        when(employeeRepository.findByUserUsername("nobody")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                ()->accountService.getAccountRequestStatusStats(principal));
        assertEquals("Employee not found",exception.getMessage());

        verify(principal,times(1)).getName();
        verify(employeeRepository,times(1)).findByUserUsername("nobody");
        verify(accountRepository,times(0)).getAccountRequestStatusStats(1L);

        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(branchService);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(principal);
    }
}
