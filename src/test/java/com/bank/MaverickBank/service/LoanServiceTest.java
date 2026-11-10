package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.LoanApproveReqDto;
import com.bank.MaverickBank.dto.request.LoanAssignReqDto;
import com.bank.MaverickBank.dto.request.LoanOpenReqDto;
import com.bank.MaverickBank.dto.request.LoanStatusStatDto;
import com.bank.MaverickBank.dto.request.LoanTypeStatDto;
import com.bank.MaverickBank.dto.response.ChartDto;
import com.bank.MaverickBank.dto.response.LoanFilterDto;
import com.bank.MaverickBank.dto.response.LoanRespDto;
import com.bank.MaverickBank.enums.JobTitle;
import com.bank.MaverickBank.enums.LoanStatus;
import com.bank.MaverickBank.enums.LoanType;
import com.bank.MaverickBank.enums.TransactionType;
import com.bank.MaverickBank.exception.OutOfReachException;
import com.bank.MaverickBank.exception.OwnershipException;
import com.bank.MaverickBank.exception.ResourceNotFoundException;
import com.bank.MaverickBank.exception.TypeMismatchException;
import com.bank.MaverickBank.model.Account;
import com.bank.MaverickBank.model.Branch;
import com.bank.MaverickBank.model.Customer;
import com.bank.MaverickBank.model.Employee;
import com.bank.MaverickBank.model.Loan;
import com.bank.MaverickBank.model.Transaction;
import com.bank.MaverickBank.repository.AccountRepository;
import com.bank.MaverickBank.repository.BranchRepository;
import com.bank.MaverickBank.repository.CustomerRepository;
import com.bank.MaverickBank.repository.EmployeeRepository;
import com.bank.MaverickBank.repository.LoanRepository;
import com.bank.MaverickBank.repository.TransactionRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
   Note: addLoan saves the verification document in to a fixed folder on the disk (uploadDir),
   so only the cases that stop before saving the document are tested here.
 */
@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {
    @InjectMocks
    private LoanService loanService;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private Principal principal;
    @Mock
    private MultipartFile verificationDocument;

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

    private Employee createEmployee(Long id,JobTitle jobTitle,Branch branch){
        Employee employee = new Employee();
        employee.setId(id);
        employee.setName("Employee" + id);
        employee.setJobTitle(jobTitle);
        employee.setBranch(branch);
        return employee;
    }

    private Account createAccount(Long id,boolean active,Customer owner,String balance){
        Account account = new Account();
        account.setId(id);
        account.setActive(active);
        account.setCustomer(owner);
        account.setBalance(new BigDecimal(balance));
        return account;
    }

    private Loan createLoan(Long id,LoanStatus status,Branch branch,Account account,Employee managingEmployee){
        Loan loan = new Loan();
        loan.setId(id);
        loan.setLoanType(LoanType.HOME);
        loan.setRequestedAmount(new BigDecimal("100000.00"));
        loan.setTenureMonths(12);
        loan.setLoanStatus(status);
        loan.setLoanBranch(branch);
        loan.setTargetAccount(account);
        loan.setManagingEmployee(managingEmployee);
        return loan;
    }

    private LoanRespDto createLoanResp(Long id,LoanStatus status){
        return new LoanRespDto(id,11L,7L,"Customer7",LoanType.HOME,new BigDecimal("100000.00"),null,12,null,null,status,
                "doc.pdf",false,1L,"MAVB0000301",null,null,LocalDate.of(2026,11,1),null,null);
    }

    private void verifyNoMoreInteractionsOnAll(){
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(loanRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(employeeRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(principal);
    }

    // ---------------------------------------------------- addLoan

    @Test
    public void addLoanTest_InvalidAccount(){
        LoanOpenReqDto dto = new LoanOpenReqDto(99L,LoanType.HOME,new BigDecimal("100000"),12,"MAVB0000301",verificationDocument);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(createCustomer(7L));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->loanService.addLoan(dto,"monisha"));
        assertEquals("Account ID is Invalid",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(99L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void addLoanTest_NotOwner(){
        LoanOpenReqDto dto = new LoanOpenReqDto(11L,LoanType.HOME,new BigDecimal("100000"),12,"MAVB0000301",verificationDocument);
        Account account = createAccount(11L,true,createCustomer(8L),"5000.00");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(createCustomer(7L));
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        OwnershipException exception = assertThrows(OwnershipException.class,
                ()->loanService.addLoan(dto,"monisha"));
        assertEquals("The Account does not belong to Customer",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void addLoanTest_InvalidIfsc(){
        LoanOpenReqDto dto = new LoanOpenReqDto(11L,LoanType.HOME,new BigDecimal("100000"),12,"MAVB0009999",verificationDocument);
        Customer customer = createCustomer(7L);
        Account account = createAccount(11L,true,customer,"5000.00");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(branchRepository.findBranchByIfscCode("MAVB0009999")).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->loanService.addLoan(dto,"monisha"));
        assertEquals("IFSC Code is Invalid",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(branchRepository,times(1)).findBranchByIfscCode("MAVB0009999");
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void addLoanTest_NoDocument(){
        LoanOpenReqDto dto = new LoanOpenReqDto(11L,LoanType.HOME,new BigDecimal("100000"),12,"MAVB0000301",null);
        Customer customer = createCustomer(7L);
        Account account = createAccount(11L,true,customer,"5000.00");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(branchRepository.findBranchByIfscCode("MAVB0000301")).thenReturn(createBranch(1L));

        assertThrows(ResourceNotFoundException.class,()->loanService.addLoan(dto,"monisha"));

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(branchRepository,times(1)).findBranchByIfscCode("MAVB0000301");
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void addLoanTest_EmptyDocument(){
        LoanOpenReqDto dto = new LoanOpenReqDto(11L,LoanType.HOME,new BigDecimal("100000"),12,"MAVB0000301",verificationDocument);
        Customer customer = createCustomer(7L);
        Account account = createAccount(11L,true,customer,"5000.00");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(branchRepository.findBranchByIfscCode("MAVB0000301")).thenReturn(createBranch(1L));
        when(verificationDocument.isEmpty()).thenReturn(true);

        assertThrows(ResourceNotFoundException.class,()->loanService.addLoan(dto,"monisha"));

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(branchRepository,times(1)).findBranchByIfscCode("MAVB0000301");
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void addLoanTest_DocumentNotPdf(){
        LoanOpenReqDto dto = new LoanOpenReqDto(11L,LoanType.HOME,new BigDecimal("100000"),12,"MAVB0000301",verificationDocument);
        Customer customer = createCustomer(7L);
        Account account = createAccount(11L,true,customer,"5000.00");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(branchRepository.findBranchByIfscCode("MAVB0000301")).thenReturn(createBranch(1L));
        when(verificationDocument.isEmpty()).thenReturn(false);
        when(verificationDocument.getContentType()).thenReturn("image/png");

        TypeMismatchException exception = assertThrows(TypeMismatchException.class,
                ()->loanService.addLoan(dto,"monisha"));
        assertEquals("Identification document must be a PDF",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(branchRepository,times(1)).findBranchByIfscCode("MAVB0000301");
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    // ---------------------------------------------------- filters

    @Test
    public void dynamicFilterLoanTest(){
        LoanRespDto dto1 = createLoanResp(1L,LoanStatus.PENDING);
        LoanRespDto dto2 = createLoanResp(2L,LoanStatus.PENDING);
        BigDecimal minRequestedAmount = new BigDecimal("5000");
        LocalDate createdFrom = LocalDate.of(2026,11,1);
        LocalDate createdTill = LocalDate.of(2026,11,30);

        when(loanRepository.dynamicFilterLoan(null,null,7L,LoanType.HOME,LoanStatus.PENDING,minRequestedAmount,null,null,null,
                "MAVB0000301",null,false,createdFrom,createdTill,null,null,null,null,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(dto1,dto2),PageRequest.of(0,5),2));

        LoanFilterDto result = loanService.dynamicFilterLoan(null,null,7L,LoanType.HOME,LoanStatus.PENDING,minRequestedAmount,null,null,null,
                "MAVB0000301",null,false,createdFrom,createdTill,null,null,null,null,0,5);

        assertEquals(2,result.loanList().size());
        assertEquals(1L,result.loanList().get(0).loanId());
        assertEquals(1,result.paginationInfo().totalPages());
        assertEquals(2L,result.paginationInfo().totalRecords());
        assertEquals(2,result.paginationInfo().numRecordsInPage());

        verify(loanRepository,times(1)).dynamicFilterLoan(null,null,7L,LoanType.HOME,LoanStatus.PENDING,minRequestedAmount,null,null,null,
                "MAVB0000301",null,false,createdFrom,createdTill,null,null,null,null,PageRequest.of(0,5));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void dynamicFilterLoanTest_Invalid(){
        //page number can not be negative, so the repository is never reached
        assertThrows(IllegalArgumentException.class,
                ()->loanService.dynamicFilterLoan(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,-1,5));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void dynamicCustomerFilterLoanTest(){
        Customer customer = createCustomer(7L);
        LoanRespDto dto1 = createLoanResp(1L,LoanStatus.APPROVED);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        //the customer id comes from the logged in customer, so a customer can only see own loans
        when(loanRepository.dynamicFilterLoan(null,null,7L,null,LoanStatus.APPROVED,null,null,null,null,
                null,null,null,null,null,null,null,null,null,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(dto1),PageRequest.of(0,5),1));

        LoanFilterDto result = loanService.dynamicCustomerFilterLoan(null,null,null,LoanStatus.APPROVED,null,null,null,null,
                null,null,null,null,null,null,null,null,null,0,5,"monisha");

        assertEquals(1,result.loanList().size());
        assertEquals(LoanStatus.APPROVED,result.loanList().get(0).loanStatus());
        assertEquals(1,result.paginationInfo().totalPages());
        assertEquals(1L,result.paginationInfo().totalRecords());
        assertEquals(1,result.paginationInfo().numRecordsInPage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(loanRepository,times(1)).dynamicFilterLoan(null,null,7L,null,LoanStatus.APPROVED,null,null,null,null,
                null,null,null,null,null,null,null,null,null,PageRequest.of(0,5));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void dynamicCustomerFilterLoanTest_Invalid(){
        Customer customer = createCustomer(7L);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);

        //page size can not be zero, so the loans are never searched
        assertThrows(IllegalArgumentException.class,
                ()->loanService.dynamicCustomerFilterLoan(null,null,null,null,null,null,null,null,
                        null,null,null,null,null,null,null,null,null,0,0,"monisha"));

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");

        verifyNoMoreInteractionsOnAll();
    }

    // ---------------------------------------------------- assignLoan

    @Test
    public void assignLoanTest(){
        Branch branch = createBranch(1L);
        Employee manager = createEmployee(1L,JobTitle.MANAGER,branch);
        Employee clerk = createEmployee(2L,JobTitle.CLERK,branch);
        Loan loan = createLoan(1L,LoanStatus.PENDING,branch,null,null);
        LoanRespDto respDto = createLoanResp(1L,LoanStatus.ASSIGNED);

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(clerk));
        when(loanRepository.getLoanById(1L)).thenReturn(respDto);

        LoanRespDto result = loanService.assignLoan(new LoanAssignReqDto(1L,2L),"mani");

        assertEquals(respDto,result);
        assertEquals(clerk,loan.getManagingEmployee());
        assertEquals(LoanStatus.ASSIGNED,loan.getLoanStatus());

        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(1L);
        verify(employeeRepository,times(1)).findById(2L);
        verify(loanRepository,times(1)).save(loan);
        verify(loanRepository,times(1)).getLoanById(1L);

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void assignLoanTest_Reassign(){
        //an ASSIGNED loan can be given to another employee of the branch
        Branch branch = createBranch(1L);
        Employee manager = createEmployee(1L,JobTitle.MANAGER,branch);
        Employee oldEmployee = createEmployee(2L,JobTitle.CLERK,branch);
        Employee newEmployee = createEmployee(3L,JobTitle.OFFICER,branch);
        Loan loan = createLoan(1L,LoanStatus.ASSIGNED,branch,null,oldEmployee);
        LoanRespDto respDto = createLoanResp(1L,LoanStatus.ASSIGNED);

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(newEmployee));
        when(loanRepository.getLoanById(1L)).thenReturn(respDto);

        loanService.assignLoan(new LoanAssignReqDto(1L,3L),"mani");

        assertEquals(newEmployee,loan.getManagingEmployee());
        assertEquals(LoanStatus.ASSIGNED,loan.getLoanStatus());

        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(1L);
        verify(employeeRepository,times(1)).findById(3L);
        verify(loanRepository,times(1)).save(loan);
        verify(loanRepository,times(1)).getLoanById(1L);

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void assignLoanTest_ManagerNotFound(){
        when(employeeRepository.getEmployeeByUsername("nobody")).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->loanService.assignLoan(new LoanAssignReqDto(1L,2L),"nobody"));
        assertEquals("Employee not found",exception.getMessage());

        verify(employeeRepository,times(1)).getEmployeeByUsername("nobody");
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void assignLoanTest_NotManager(){
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);

        OwnershipException exception = assertThrows(OwnershipException.class,
                ()->loanService.assignLoan(new LoanAssignReqDto(1L,2L),"clara"));
        assertEquals("Only a Manager can assign loans",exception.getMessage());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void assignLoanTest_InvalidLoan(){
        Employee manager = createEmployee(1L,JobTitle.MANAGER,createBranch(1L));

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->loanService.assignLoan(new LoanAssignReqDto(99L,2L),"mani"));
        assertEquals("Loan ID is Invalid",exception.getMessage());

        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(99L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void assignLoanTest_WrongStatus(){
        Branch branch = createBranch(1L);
        Employee manager = createEmployee(1L,JobTitle.MANAGER,branch);
        Loan loan = createLoan(1L,LoanStatus.APPROVED,branch,null,null);

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        TypeMismatchException exception = assertThrows(TypeMismatchException.class,
                ()->loanService.assignLoan(new LoanAssignReqDto(1L,2L),"mani"));
        assertEquals("Only a PENDING or ASSIGNED loan can be assigned",exception.getMessage());
        assertEquals(LoanStatus.APPROVED,loan.getLoanStatus());

        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void assignLoanTest_ManagerOfOtherBranch(){
        Employee manager = createEmployee(1L,JobTitle.MANAGER,createBranch(2L));
        Loan loan = createLoan(1L,LoanStatus.PENDING,createBranch(1L),null,null);

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        OwnershipException exception = assertThrows(OwnershipException.class,
                ()->loanService.assignLoan(new LoanAssignReqDto(1L,2L),"mani"));
        assertEquals("The Loan does not belong to your Branch",exception.getMessage());

        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void assignLoanTest_InvalidEmployee(){
        Branch branch = createBranch(1L);
        Employee manager = createEmployee(1L,JobTitle.MANAGER,branch);
        Loan loan = createLoan(1L,LoanStatus.PENDING,branch,null,null);

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->loanService.assignLoan(new LoanAssignReqDto(1L,99L),"mani"));
        assertEquals("Employee ID is Invalid",exception.getMessage());

        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(1L);
        verify(employeeRepository,times(1)).findById(99L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void assignLoanTest_EmployeeOfOtherBranch(){
        Branch branch = createBranch(1L);
        Employee manager = createEmployee(1L,JobTitle.MANAGER,branch);
        Employee otherBranchEmployee = createEmployee(2L,JobTitle.CLERK,createBranch(2L));
        Loan loan = createLoan(1L,LoanStatus.PENDING,branch,null,null);

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(otherBranchEmployee));

        OwnershipException exception = assertThrows(OwnershipException.class,
                ()->loanService.assignLoan(new LoanAssignReqDto(1L,2L),"mani"));
        assertEquals("The Employee does not belong to the Loan Branch",exception.getMessage());
        assertNull(loan.getManagingEmployee());

        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(1L);
        verify(employeeRepository,times(1)).findById(2L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    // ---------------------------------------------------- approveLoan

    @Test
    public void approveLoanTest(){
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));
        Loan loan = createLoan(1L,LoanStatus.ASSIGNED,createBranch(1L),null,clerk);
        LoanRespDto respDto = createLoanResp(1L,LoanStatus.APPROVED);

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.getLoanById(1L)).thenReturn(respDto);

        LoanRespDto result = loanService.approveLoan(
                new LoanApproveReqDto(1L,new BigDecimal("100000.00"),new BigDecimal("12.00")),"clara");

        assertEquals(respDto,result);
        assertEquals(LoanStatus.APPROVED,loan.getLoanStatus());
        assertTrue(loan.isFinancialVerification());
        assertEquals(new BigDecimal("100000.00"),loan.getApprovedAmount());
        assertEquals(new BigDecimal("12.00"),loan.getInterestRate());
        //EMI of 1,00,000 at 12% a year for 12 months
        assertEquals(new BigDecimal("8884.88"),loan.getEmi());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(1)).save(loan);
        verify(loanRepository,times(1)).getLoanById(1L);

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void approveLoanTest_LowerAmount(){
        //the approved amount can be lower than the requested amount
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));
        Loan loan = createLoan(1L,LoanStatus.ASSIGNED,createBranch(1L),null,clerk);
        LoanRespDto respDto = createLoanResp(1L,LoanStatus.APPROVED);

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.getLoanById(1L)).thenReturn(respDto);

        loanService.approveLoan(new LoanApproveReqDto(1L,new BigDecimal("50000.00"),new BigDecimal("12.00")),"clara");

        assertEquals(new BigDecimal("50000.00"),loan.getApprovedAmount());
        assertEquals(new BigDecimal("4442.44"),loan.getEmi());
        assertEquals(LoanStatus.APPROVED,loan.getLoanStatus());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(1)).save(loan);
        verify(loanRepository,times(1)).getLoanById(1L);

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void approveLoanTest_EmployeeNotFound(){
        when(employeeRepository.getEmployeeByUsername("nobody")).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->loanService.approveLoan(new LoanApproveReqDto(1L,new BigDecimal("1000"),new BigDecimal("10")),"nobody"));
        assertEquals("Employee not found",exception.getMessage());

        verify(employeeRepository,times(1)).getEmployeeByUsername("nobody");
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void approveLoanTest_InvalidLoan(){
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->loanService.approveLoan(new LoanApproveReqDto(99L,new BigDecimal("1000"),new BigDecimal("10")),"clara"));
        assertEquals("Loan ID is Invalid",exception.getMessage());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(loanRepository,times(1)).findById(99L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void approveLoanTest_WrongStatus(){
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));
        Loan loan = createLoan(1L,LoanStatus.PENDING,createBranch(1L),null,clerk);

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        OutOfReachException exception = assertThrows(OutOfReachException.class,
                ()->loanService.approveLoan(new LoanApproveReqDto(1L,new BigDecimal("1000"),new BigDecimal("10")),"clara"));
        assertEquals("Only a ASSIGNED loan can be used for this action",exception.getMessage());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void approveLoanTest_NotAssignedToEmployee(){
        Branch branch = createBranch(1L);
        Employee clerk = createEmployee(2L,JobTitle.CLERK,branch);
        Employee otherEmployee = createEmployee(3L,JobTitle.OFFICER,branch);
        Loan loan = createLoan(1L,LoanStatus.ASSIGNED,branch,null,otherEmployee);

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        OwnershipException exception = assertThrows(OwnershipException.class,
                ()->loanService.approveLoan(new LoanApproveReqDto(1L,new BigDecimal("1000"),new BigDecimal("10")),"clara"));
        assertEquals("The Loan is not assigned to you",exception.getMessage());
        assertEquals(LoanStatus.ASSIGNED,loan.getLoanStatus());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void approveLoanTest_AmountMoreThanRequested(){
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));
        Loan loan = createLoan(1L,LoanStatus.ASSIGNED,createBranch(1L),null,clerk);

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        TypeMismatchException exception = assertThrows(TypeMismatchException.class,
                ()->loanService.approveLoan(new LoanApproveReqDto(1L,new BigDecimal("100000.01"),new BigDecimal("12.00")),"clara"));
        assertEquals("Approved Amount cannot be more than the Requested Amount",exception.getMessage());
        assertEquals(LoanStatus.ASSIGNED,loan.getLoanStatus());
        assertNull(loan.getApprovedAmount());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    // ---------------------------------------------------- rejectLoan

    @Test
    public void rejectLoanTest(){
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));
        Loan loan = createLoan(1L,LoanStatus.ASSIGNED,createBranch(1L),null,clerk);
        LoanRespDto respDto = createLoanResp(1L,LoanStatus.REJECTED);

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.getLoanById(1L)).thenReturn(respDto);

        LoanRespDto result = loanService.rejectLoan(1L,"clara");

        assertEquals(respDto,result);
        assertEquals(LoanStatus.REJECTED,loan.getLoanStatus());
        assertTrue(loan.isFinancialVerification());
        assertEquals(LocalDate.now(),loan.getClosedAt());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(1)).save(loan);
        verify(loanRepository,times(1)).getLoanById(1L);

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void rejectLoanTest_WrongStatus(){
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));
        Loan loan = createLoan(1L,LoanStatus.APPROVED,createBranch(1L),null,clerk);

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThrows(OutOfReachException.class,()->loanService.rejectLoan(1L,"clara"));
        assertEquals(LoanStatus.APPROVED,loan.getLoanStatus());
        assertNull(loan.getClosedAt());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void rejectLoanTest_NotAssignedToEmployee(){
        Branch branch = createBranch(1L);
        Employee clerk = createEmployee(2L,JobTitle.CLERK,branch);
        Loan loan = createLoan(1L,LoanStatus.ASSIGNED,branch,null,null);

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThrows(OwnershipException.class,()->loanService.rejectLoan(1L,"clara"));
        assertEquals(LoanStatus.ASSIGNED,loan.getLoanStatus());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    // ---------------------------------------------------- acceptLoan / customerRejectLoan

    @Test
    public void acceptLoanTest(){
        Customer customer = createCustomer(7L);
        Account account = createAccount(11L,true,customer,"5000.00");
        Loan loan = createLoan(1L,LoanStatus.APPROVED,createBranch(1L),account,null);
        LoanRespDto respDto = createLoanResp(1L,LoanStatus.ACCEPTED);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.getLoanById(1L)).thenReturn(respDto);

        LoanRespDto result = loanService.acceptLoan(1L,"monisha");

        assertEquals(respDto,result);
        assertEquals(LoanStatus.ACCEPTED,loan.getLoanStatus());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(1)).save(loan);
        verify(loanRepository,times(1)).getLoanById(1L);

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void acceptLoanTest_InvalidLoan(){
        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(createCustomer(7L));
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->loanService.acceptLoan(99L,"monisha"));
        assertEquals("Loan ID is Invalid",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(loanRepository,times(1)).findById(99L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void acceptLoanTest_NotOwner(){
        Account account = createAccount(11L,true,createCustomer(8L),"5000.00");
        Loan loan = createLoan(1L,LoanStatus.APPROVED,createBranch(1L),account,null);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(createCustomer(7L));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        OwnershipException exception = assertThrows(OwnershipException.class,
                ()->loanService.acceptLoan(1L,"monisha"));
        assertEquals("The Loan does not belong to Customer",exception.getMessage());
        assertEquals(LoanStatus.APPROVED,loan.getLoanStatus());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void acceptLoanTest_NotApproved(){
        Customer customer = createCustomer(7L);
        Account account = createAccount(11L,true,customer,"5000.00");
        Loan loan = createLoan(1L,LoanStatus.PENDING,createBranch(1L),account,null);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        OutOfReachException exception = assertThrows(OutOfReachException.class,
                ()->loanService.acceptLoan(1L,"monisha"));
        assertEquals("Only an APPROVED loan can be accepted or rejected",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void customerRejectLoanTest(){
        Customer customer = createCustomer(7L);
        Account account = createAccount(11L,true,customer,"5000.00");
        Loan loan = createLoan(1L,LoanStatus.APPROVED,createBranch(1L),account,null);
        LoanRespDto respDto = createLoanResp(1L,LoanStatus.REJECTED);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.getLoanById(1L)).thenReturn(respDto);

        LoanRespDto result = loanService.customerRejectLoan(1L,"monisha");

        assertEquals(respDto,result);
        assertEquals(LoanStatus.REJECTED,loan.getLoanStatus());
        assertEquals(LocalDate.now(),loan.getClosedAt());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(1)).save(loan);
        verify(loanRepository,times(1)).getLoanById(1L);

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void customerRejectLoanTest_NotApproved(){
        Customer customer = createCustomer(7L);
        Account account = createAccount(11L,true,customer,"5000.00");
        Loan loan = createLoan(1L,LoanStatus.ONGOING,createBranch(1L),account,null);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThrows(OutOfReachException.class,()->loanService.customerRejectLoan(1L,"monisha"));
        assertEquals(LoanStatus.ONGOING,loan.getLoanStatus());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    // ---------------------------------------------------- disburseLoan / managerRejectLoan

    @Test
    public void disburseLoanTest(){
        Branch branch = createBranch(1L);
        Employee manager = createEmployee(1L,JobTitle.MANAGER,branch);
        Account account = createAccount(11L,true,createCustomer(7L),"5000.00");
        Loan loan = createLoan(1L,LoanStatus.ACCEPTED,branch,account,null);
        loan.setApprovedAmount(new BigDecimal("90000.00"));
        LoanRespDto respDto = createLoanResp(1L,LoanStatus.ONGOING);

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.getLoanById(1L)).thenReturn(respDto);

        LoanRespDto result = loanService.disburseLoan(1L,"mani");

        assertEquals(respDto,result);
        //the approved amount is added to the account
        assertEquals(new BigDecimal("95000.00"),account.getBalance());
        assertEquals(LoanStatus.ONGOING,loan.getLoanStatus());
        assertEquals(LocalDate.now(),loan.getDisbursedAt());

        //and a completed deposit transaction is recorded, so that the statement shows the loan amount
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(1L);
        verify(accountRepository,times(1)).save(account);
        verify(transactionRepository,times(1)).save(captor.capture());
        verify(loanRepository,times(1)).save(loan);
        verify(loanRepository,times(1)).getLoanById(1L);
        Transaction transaction = captor.getValue();
        assertEquals(TransactionType.DEPOSIT,transaction.getTransactionType());
        assertEquals(account,transaction.getDestinationAccount());
        assertEquals(new BigDecimal("90000.00"),transaction.getAmount());
        assertEquals(new BigDecimal("95000.00"),transaction.getDestinationAccountBalance());
        assertTrue(transaction.isCompleted());

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void disburseLoanTest_NotManager(){
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);

        OwnershipException exception = assertThrows(OwnershipException.class,()->loanService.disburseLoan(1L,"clara"));
        assertEquals("Only a Manager can perform this action",exception.getMessage());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void disburseLoanTest_WrongStatus(){
        Branch branch = createBranch(1L);
        Employee manager = createEmployee(1L,JobTitle.MANAGER,branch);
        Account account = createAccount(11L,true,createCustomer(7L),"5000.00");
        Loan loan = createLoan(1L,LoanStatus.APPROVED,branch,account,null);

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        //the customer has not accepted the loan yet
        OutOfReachException exception = assertThrows(OutOfReachException.class,()->loanService.disburseLoan(1L,"mani"));
        assertEquals("Only an ACCEPTED loan can be disbursed or rejected",exception.getMessage());
        assertEquals(new BigDecimal("5000.00"),account.getBalance());

        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(1L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void disburseLoanTest_ManagerOfOtherBranch(){
        Employee manager = createEmployee(1L,JobTitle.MANAGER,createBranch(2L));
        Account account = createAccount(11L,true,createCustomer(7L),"5000.00");
        Loan loan = createLoan(1L,LoanStatus.ACCEPTED,createBranch(1L),account,null);

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        OwnershipException exception = assertThrows(OwnershipException.class,()->loanService.disburseLoan(1L,"mani"));
        assertEquals("The Loan does not belong to your Branch",exception.getMessage());
        assertEquals(new BigDecimal("5000.00"),account.getBalance());

        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(1L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void disburseLoanTest_InactiveAccount(){
        Branch branch = createBranch(1L);
        Employee manager = createEmployee(1L,JobTitle.MANAGER,branch);
        Account account = createAccount(11L,false,createCustomer(7L),"5000.00");
        Loan loan = createLoan(1L,LoanStatus.ACCEPTED,branch,account,null);
        loan.setApprovedAmount(new BigDecimal("90000.00"));

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        TypeMismatchException exception = assertThrows(TypeMismatchException.class,()->loanService.disburseLoan(1L,"mani"));
        assertEquals("The Target Account is not active",exception.getMessage());
        assertEquals(new BigDecimal("5000.00"),account.getBalance());
        assertEquals(LoanStatus.ACCEPTED,loan.getLoanStatus());

        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(1L);
        verify(accountRepository,times(0)).save(any(Account.class));
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void managerRejectLoanTest(){
        Branch branch = createBranch(1L);
        Employee manager = createEmployee(1L,JobTitle.MANAGER,branch);
        Loan loan = createLoan(1L,LoanStatus.ACCEPTED,branch,null,null);
        LoanRespDto respDto = createLoanResp(1L,LoanStatus.REJECTED);

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.getLoanById(1L)).thenReturn(respDto);

        LoanRespDto result = loanService.managerRejectLoan(1L,"mani");

        assertEquals(respDto,result);
        assertEquals(LoanStatus.REJECTED,loan.getLoanStatus());
        assertEquals(LocalDate.now(),loan.getClosedAt());

        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(1)).save(loan);
        verify(loanRepository,times(1)).getLoanById(1L);

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void managerRejectLoanTest_WrongStatus(){
        Branch branch = createBranch(1L);
        Employee manager = createEmployee(1L,JobTitle.MANAGER,branch);
        Loan loan = createLoan(1L,LoanStatus.ONGOING,branch,null,null);

        when(employeeRepository.getEmployeeByUsername("mani")).thenReturn(manager);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThrows(OutOfReachException.class,()->loanService.managerRejectLoan(1L,"mani"));
        assertEquals(LoanStatus.ONGOING,loan.getLoanStatus());

        verify(employeeRepository,times(1)).getEmployeeByUsername("mani");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    // ---------------------------------------------------- completeLoan

    @Test
    public void completeLoanTest(){
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));
        Loan loan = createLoan(1L,LoanStatus.ONGOING,createBranch(1L),null,clerk);
        LoanRespDto respDto = createLoanResp(1L,LoanStatus.COMPLETED);

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.getLoanById(1L)).thenReturn(respDto);

        LoanRespDto result = loanService.completeLoan(1L,"clara");

        assertEquals(respDto,result);
        assertEquals(LoanStatus.COMPLETED,loan.getLoanStatus());
        assertEquals(LocalDate.now(),loan.getClosedAt());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(1)).save(loan);
        verify(loanRepository,times(1)).getLoanById(1L);

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void completeLoanTest_WrongStatus(){
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));
        Loan loan = createLoan(1L,LoanStatus.ASSIGNED,createBranch(1L),null,clerk);

        when(employeeRepository.getEmployeeByUsername("clara")).thenReturn(clerk);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        OutOfReachException exception = assertThrows(OutOfReachException.class,()->loanService.completeLoan(1L,"clara"));
        assertEquals("Only a ONGOING loan can be used for this action",exception.getMessage());
        assertEquals(LoanStatus.ASSIGNED,loan.getLoanStatus());

        verify(employeeRepository,times(1)).getEmployeeByUsername("clara");
        verify(loanRepository,times(1)).findById(1L);
        verify(loanRepository,times(0)).save(any(Loan.class));

        verifyNoMoreInteractionsOnAll();
    }

    // ---------------------------------------------------- stats for the charts

    @Test
    public void getLoanStatusStatsTest(){
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));

        when(principal.getName()).thenReturn("clara");
        when(employeeRepository.findByUserUsername("clara")).thenReturn(Optional.of(clerk));
        when(loanRepository.getLoanStatusStats(1L))
                .thenReturn(List.of(new LoanStatusStatDto(LoanStatus.PENDING,5L),new LoanStatusStatDto(LoanStatus.ONGOING,2L)));

        ChartDto result = loanService.getLoanStatusStats(principal);

        assertEquals(List.of("PENDING","ONGOING"),result.labels());
        assertEquals(List.of(5L,2L),result.values());

        verify(principal,times(1)).getName();
        verify(employeeRepository,times(1)).findByUserUsername("clara");
        verify(loanRepository,times(1)).getLoanStatusStats(1L);

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void getLoanStatusStatsTest_Invalid(){
        when(principal.getName()).thenReturn("nobody");
        when(employeeRepository.findByUserUsername("nobody")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,()->loanService.getLoanStatusStats(principal));
        assertEquals("Employee not found",exception.getMessage());

        verify(principal,times(1)).getName();
        verify(employeeRepository,times(1)).findByUserUsername("nobody");
        verify(loanRepository,times(0)).getLoanStatusStats(anyLong());

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void getLoanTypeStatsTest(){
        Employee clerk = createEmployee(2L,JobTitle.CLERK,createBranch(1L));

        when(principal.getName()).thenReturn("clara");
        when(employeeRepository.findByUserUsername("clara")).thenReturn(Optional.of(clerk));
        when(loanRepository.getLoanTypeStats(1L))
                .thenReturn(List.of(new LoanTypeStatDto(LoanType.HOME,3L),new LoanTypeStatDto(LoanType.GOLD,1L)));

        ChartDto result = loanService.getLoanTypeStats(principal);

        assertEquals(List.of("HOME","GOLD"),result.labels());
        assertEquals(List.of(3L,1L),result.values());

        verify(principal,times(1)).getName();
        verify(employeeRepository,times(1)).findByUserUsername("clara");
        verify(loanRepository,times(1)).getLoanTypeStats(1L);

        verifyNoMoreInteractionsOnAll();
    }

    @Test
    public void getLoanTypeStatsTest_Invalid(){
        when(principal.getName()).thenReturn("nobody");
        when(employeeRepository.findByUserUsername("nobody")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,()->loanService.getLoanTypeStats(principal));
        assertEquals("Employee not found",exception.getMessage());

        verify(principal,times(1)).getName();
        verify(employeeRepository,times(1)).findByUserUsername("nobody");
        verify(loanRepository,times(0)).getLoanTypeStats(anyLong());

        verifyNoMoreInteractionsOnAll();
    }
}
