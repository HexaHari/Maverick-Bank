package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.*;
import com.bank.MaverickBank.dto.response.ChartDto;
import com.bank.MaverickBank.dto.response.LoanFilterDto;
import com.bank.MaverickBank.dto.response.LoanRespDto;
import com.bank.MaverickBank.dto.response.PaginationDto;
import com.bank.MaverickBank.enums.JobTitle;
import com.bank.MaverickBank.enums.LoanStatus;
import com.bank.MaverickBank.enums.LoanType;
import com.bank.MaverickBank.enums.TransactionType;
import com.bank.MaverickBank.exception.OutOfReachException;
import com.bank.MaverickBank.exception.OwnershipException;
import com.bank.MaverickBank.exception.ResourceNotFoundException;
import com.bank.MaverickBank.exception.TypeMismatchException;
import com.bank.MaverickBank.model.*;
import com.bank.MaverickBank.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoanService {
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;
    private final TransactionRepository transactionRepository;
    private final static String uploadDir ="C:/Users/srih8/NodeJavaScriptFsd/MavericBank-ui/public/uploads/";

    public LoanRespDto addLoan(@Valid LoanOpenReqDto loanOpenReqDto, String customerUsername) throws IOException {
        log.info("Adding Loan for Customer");
        //Getting loggedIn Customer ID for Validation
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        //Getting the specified account throw Exception if it does not Exist
        Account account = accountRepository.findById(loanOpenReqDto.targetAccountId())
                .orElseThrow(()-> new ResourceNotFoundException("Account ID is Invalid"));
        //Checking if the account belongs to the Customer else throw Exception
        if(!(account.getCustomer().getId().equals(customer.getId()))){
            throw new OwnershipException("The Account does not belong to Customer");
        }
        Branch branch = Optional.ofNullable(branchRepository.findBranchByIfscCode(loanOpenReqDto.ifscCode()))
                .orElseThrow(() -> new ResourceNotFoundException("IFSC Code is Invalid"));
        if( loanOpenReqDto.verificationDocument() ==null || loanOpenReqDto.verificationDocument().isEmpty())
            throw new ResourceNotFoundException("Verification document is required to update the profile");
        if (!"application/pdf".equals(loanOpenReqDto.verificationDocument().getContentType())) {
            throw new TypeMismatchException("Identification document must be a PDF");
        }
        //Creating loan object and adding required filed values
        Loan loan = new Loan();
        loan.setTargetAccount(account);
        loan.setLoanType(loanOpenReqDto.loanType());
        loan.setRequestedAmount(loanOpenReqDto.requestedAmount());
        loan.setTenureMonths(loanOpenReqDto.tenureMonths());
        loan.setLoanBranch(branch);
        loan.setLoanStatus(LoanStatus.PENDING);
        loan.setVerificationDocument(saveFile(loanOpenReqDto.verificationDocument()));
        //Saving the loan entity
        loan = loanRepository.save(loan);
        log.info("Loan is added and returning related Info");
        return loanRepository.getLoanById(loan.getId());
    }

    private String saveFile(MultipartFile file) throws IOException{
        log.info("Uploaded File Name: {}", file.getOriginalFilename());
        log.info("File Size: {}", file.getSize());
        String ext = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];
        log.info("File Extension: {}", ext);
        String originalFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), Paths.get(uploadDir+originalFileName), StandardCopyOption.REPLACE_EXISTING);
        return originalFileName;
    }

    public LoanFilterDto dynamicFilterLoan(Long loanId, Long targetAccountId, Long customerId, LoanType loanType,
                                           LoanStatus loanStatus,
                                           BigDecimal minRequestedAmount, BigDecimal maxRequestedAmount,
                                           BigDecimal minApprovedAmount, BigDecimal maxApprovedAmount,
                                           String ifscCode, Long managingEmployeeId, Boolean financialVerification,
                                           LocalDate createdFrom, LocalDate createdTill,
                                           LocalDate disbursedFrom, LocalDate disbursedTill,
                                           LocalDate closedFrom, LocalDate closedTill,
                                           Integer page, Integer size) {
        log.info("Searching Loans through Dynamic Filter");
        //Creating Pageable object for Pagination
        Pageable pageable = PageRequest.of(page, size);
        log.info("Returning Filtered Loans");
        //filtering the loans in the repository and returning them
        Page<LoanRespDto> pageDto = loanRepository.dynamicFilterLoan(loanId, targetAccountId, customerId, loanType,
                loanStatus, minRequestedAmount, maxRequestedAmount, minApprovedAmount,
                maxApprovedAmount, ifscCode, managingEmployeeId, financialVerification, createdFrom, createdTill,
                disbursedFrom, disbursedTill, closedFrom, closedTill, pageable);
        return new LoanFilterDto(pageDto.getContent(),
                new PaginationDto(pageDto.getTotalPages(), pageDto.getTotalElements(), pageDto.getNumberOfElements()));
    }

    public LoanFilterDto dynamicCustomerFilterLoan(Long loanId, Long targetAccountId, LoanType loanType,
                                           LoanStatus loanStatus,
                                           BigDecimal minRequestedAmount, BigDecimal maxRequestedAmount,
                                           BigDecimal minApprovedAmount, BigDecimal maxApprovedAmount,
                                           String ifscCode, Long managingEmployeeId, Boolean financialVerification,
                                           LocalDate createdFrom, LocalDate createdTill,
                                           LocalDate disbursedFrom, LocalDate disbursedTill,
                                           LocalDate closedFrom, LocalDate closedTill,
                                           Integer page, Integer size, String customerUsername) {
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        log.info("Searching Loans through Dynamic Filter");
        //Creating Pageable object for Pagination
        Pageable pageable = PageRequest.of(page, size);
        log.info("Returning Filtered Loans");
        //filtering the loans in the repository and returning them
        Page<LoanRespDto> pageDto = loanRepository.dynamicFilterLoan(loanId, targetAccountId, customer.getId(), loanType,
                loanStatus,  minRequestedAmount, maxRequestedAmount, minApprovedAmount,
                maxApprovedAmount, ifscCode, managingEmployeeId, financialVerification, createdFrom, createdTill,
                disbursedFrom, disbursedTill, closedFrom, closedTill, pageable);
        return new LoanFilterDto(pageDto.getContent(),
                new PaginationDto(pageDto.getTotalPages(), pageDto.getTotalElements(), pageDto.getNumberOfElements()));
    }

    public LoanRespDto assignLoan(@Valid LoanAssignReqDto loanAssignReqDto, String managerUsername) {
        log.info("Assigning Loan to an Employee");
        //Getting loggedIn Employee for Validation
        Employee manager = Optional.ofNullable(employeeRepository.getEmployeeByUsername(managerUsername))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        //Only a Manager can assign loans
        if (manager.getJobTitle() != JobTitle.MANAGER) {
            throw new OwnershipException("Only a Manager can assign loans");
        }
        //Getting the specified loan throw Exception if it does not Exist
        Loan loan = loanRepository.findById(loanAssignReqDto.loanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan ID is Invalid"));
        //Only pending or assigned loans can be (re)assigned
        if (loan.getLoanStatus() != LoanStatus.PENDING && loan.getLoanStatus() != LoanStatus.ASSIGNED) {
            throw new TypeMismatchException("Only a PENDING or ASSIGNED loan can be assigned");
        }
        //Manager must belong to the loan branch
        if (manager.getBranch() == null || !manager.getBranch().getId().equals(loan.getLoanBranch().getId())) {
            throw new OwnershipException("The Loan does not belong to your Branch");
        }
        //Getting the employee who will handle the loan
        Employee employee = employeeRepository.findById(loanAssignReqDto.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee ID is Invalid"));
        //The employee must also belong to the loan branch
        if (employee.getBranch() == null || !employee.getBranch().getId().equals(loan.getLoanBranch().getId())) {
            throw new OwnershipException("The Employee does not belong to the Loan Branch");
        }
        //Assigning the loan and changing the status
        loan.setManagingEmployee(employee);
        loan.setLoanStatus(LoanStatus.ASSIGNED);
        loanRepository.save(loan);
        log.info("Loan is assigned and returning related Info");
        return loanRepository.getLoanById(loan.getId());
    }

    public LoanRespDto approveLoan(@Valid LoanApproveReqDto loanApproveReqDto, String employeeUsername) {
        log.info("Approving Loan");
        Loan loan = getAssignedLoan(loanApproveReqDto.loanId(), employeeUsername, LoanStatus.ASSIGNED);
        //Approved amount can be lower than the requested amount but never higher
        if (loanApproveReqDto.approvedAmount().compareTo(loan.getRequestedAmount()) > 0) {
            throw new TypeMismatchException("Approved Amount cannot be more than the Requested Amount");
        }
        loan.setFinancialVerification(true);
        loan.setApprovedAmount(loanApproveReqDto.approvedAmount());
        loan.setInterestRate(loanApproveReqDto.interestRate());
        loan.setEmi(calculateEmi(loanApproveReqDto.approvedAmount(), loanApproveReqDto.interestRate(),
                loan.getTenureMonths()));
        loan.setLoanStatus(LoanStatus.APPROVED);
        loanRepository.save(loan);
        log.info("Loan is approved and returning related Info");
        return loanRepository.getLoanById(loan.getId());
    }

    public LoanRespDto rejectLoan(Long loanId, String employeeUsername) {
        log.info("Rejecting Loan");
        Loan loan = getAssignedLoan(loanId, employeeUsername, LoanStatus.ASSIGNED);
        loan.setFinancialVerification(true);
        loan.setLoanStatus(LoanStatus.REJECTED);
        loan.setClosedAt(LocalDate.now());
        loanRepository.save(loan);
        log.info("Loan is rejected and returning related Info");
        return loanRepository.getLoanById(loan.getId());
    }

    private Loan getAssignedLoan(Long loanId, String employeeUsername, LoanStatus requiredStatus) {
        //Getting loggedIn Employee for Validation
        Employee employee = Optional.ofNullable(employeeRepository.getEmployeeByUsername(employeeUsername))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        //Getting the specified loan throw Exception if it does not Exist
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan ID is Invalid"));
        //The loan must be in the status required for this action
        if (loan.getLoanStatus() != requiredStatus) {
            throw new OutOfReachException("Only a " + requiredStatus + " loan can be used for this action");
        }
        //Only the employee managing the loan can act on it
        if (loan.getManagingEmployee() == null || !loan.getManagingEmployee().getId().equals(employee.getId())) {
            throw new OwnershipException("The Loan is not assigned to you");
        }
        return loan;
    }

    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, Integer months) {
        //EMI = P * r * (1+r)^n / ((1+r)^n - 1) where r is the monthly rate
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal factor = BigDecimal.ONE.add(monthlyRate).pow(months, new MathContext(20));
        return principal.multiply(monthlyRate).multiply(factor)
                .divide(factor.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }

    public LoanRespDto acceptLoan(Long loanId, String customerUsername) {
        log.info("Customer is Accepting the Loan");
        Loan loan = getCustomerLoan(loanId, customerUsername);
        loan.setLoanStatus(LoanStatus.ACCEPTED);
        loanRepository.save(loan);
        log.info("Loan is accepted and returning related Info");
        return loanRepository.getLoanById(loan.getId());
    }

    private Loan getCustomerLoan(Long loanId, String customerUsername) {
        //Getting loggedIn Customer for Validation
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        //Getting the specified loan throw Exception if it does not Exist
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan ID is Invalid"));
        //Checking if the loan belongs to the Customer else throw Exception
        if (!loan.getTargetAccount().getCustomer().getId().equals(customer.getId())) {
            throw new OwnershipException("The Loan does not belong to Customer");
        }
        //Customer can only decide on an approved loan
        if (loan.getLoanStatus() != LoanStatus.APPROVED) {
            throw new OutOfReachException("Only an APPROVED loan can be accepted or rejected");
        }
        return loan;
    }

    public LoanRespDto customerRejectLoan(Long loanId, String customerUsername) {
        log.info("Customer is Rejecting the Loan");
        Loan loan = getCustomerLoan(loanId, customerUsername);
        loan.setLoanStatus(LoanStatus.REJECTED);
        loan.setClosedAt(LocalDate.now());
        loanRepository.save(loan);
        log.info("Loan is rejected by Customer and returning related Info");
        return loanRepository.getLoanById(loan.getId());
    }

    @Transactional
    public LoanRespDto disburseLoan(Long loanId, String managerUsername) {
        log.info("Disbursing the Loan");
        Loan loan = getManagerLoan(loanId, managerUsername);
        Account account = loan.getTargetAccount();
        //Money can only be added to an active account
        if (!account.isActive()) {
            throw new TypeMismatchException("The Target Account is not active");
        }
        //Adding the approved amount to the account balance (no limit for loan disbursal)
        account.setBalance(account.getBalance().add(loan.getApprovedAmount()));
        accountRepository.save(account);
        //Creating a deposit Transaction with the balance snapshot so statements stay correct
        Transaction transaction = new Transaction();
        transaction.setDestinationAccount(account);
        transaction.setAmount(loan.getApprovedAmount());
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setDestinationAccountBalance(account.getBalance());
        transaction.setCompleted(true);
        transactionRepository.save(transaction);
        //Updating the loan
        loan.setLoanStatus(LoanStatus.ONGOING);
        loan.setDisbursedAt(LocalDate.now());
        loanRepository.save(loan);
        log.info("Loan is disbursed and returning related Info");
        return loanRepository.getLoanById(loan.getId());
    }

    private Loan getManagerLoan(Long loanId, String managerUsername) {
        //Getting loggedIn Employee for Validation
        Employee manager = Optional.ofNullable(employeeRepository.getEmployeeByUsername(managerUsername))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        //Only a Manager can perform this action
        if (manager.getJobTitle() != JobTitle.MANAGER) {
            throw new OwnershipException("Only a Manager can perform this action");
        }
        //Getting the specified loan throw Exception if it does not Exist
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan ID is Invalid"));
        //Only an accepted loan can be disbursed or rejected
        if (loan.getLoanStatus() != LoanStatus.ACCEPTED) {
            throw new OutOfReachException("Only an ACCEPTED loan can be disbursed or rejected");
        }
        //Manager must belong to the loan branch
        if (manager.getBranch() == null || !manager.getBranch().getId().equals(loan.getLoanBranch().getId())) {
            throw new OwnershipException("The Loan does not belong to your Branch");
        }
        return loan;
    }

    public LoanRespDto managerRejectLoan(Long loanId, String managerUsername) {
        log.info("Manager is Rejecting the Accepted Loan");
        Loan loan = getManagerLoan(loanId, managerUsername);
        loan.setLoanStatus(LoanStatus.REJECTED);
        loan.setClosedAt(LocalDate.now());
        loanRepository.save(loan);
        log.info("Loan is rejected by Manager and returning related Info");
        return loanRepository.getLoanById(loan.getId());
    }

    public LoanRespDto completeLoan(Long loanId, String employeeUsername) {
        log.info("Closing the repaid Loan");
        Loan loan = getAssignedLoan(loanId, employeeUsername, LoanStatus.ONGOING);
        loan.setLoanStatus(LoanStatus.COMPLETED);
        loan.setClosedAt(LocalDate.now());
        loanRepository.save(loan);
        log.info("Loan is completed and returning related Info");
        return loanRepository.getLoanById(loan.getId());
    }

    public ChartDto getLoanStatusStats(Principal principal) {
        Employee employee = employeeRepository.findByUserUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        List<LoanStatusStatDto> list = loanRepository.getLoanStatusStats(employee.getBranch().getId());

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        list.forEach(e -> {
            labels.add(e.loanStatus().toString());
            values.add(e.numberOfLoans());
        });
        return new ChartDto(labels, values);
    }

    public ChartDto getLoanTypeStats(Principal principal) {
        Employee employee = employeeRepository.findByUserUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        List<LoanTypeStatDto> list = loanRepository.getLoanTypeStats(employee.getBranch().getId());

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        list.forEach(e -> {
            labels.add(e.loanType().toString());
            values.add(e.numberOfLoans());
        });
        return new ChartDto(labels, values);
    }
}
