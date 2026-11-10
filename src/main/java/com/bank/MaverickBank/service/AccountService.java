package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.AccountCloseReqDto;
import com.bank.MaverickBank.dto.request.AccountOpenReqDto;
import com.bank.MaverickBank.dto.response.*;
import com.bank.MaverickBank.enums.AccountType;
import com.bank.MaverickBank.enums.JobTitle;
import com.bank.MaverickBank.enums.RequestStatus;
import com.bank.MaverickBank.exception.*;
import com.bank.MaverickBank.mapper.AccountMapper;
import com.bank.MaverickBank.model.Account;
import com.bank.MaverickBank.model.Branch;
import com.bank.MaverickBank.model.Customer;
import com.bank.MaverickBank.model.Employee;
import com.bank.MaverickBank.repository.AccountRepository;
import com.bank.MaverickBank.repository.CustomerRepository;
import com.bank.MaverickBank.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final BranchService branchService;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public void addAccount(AccountOpenReqDto accountOpenReqDto, String customerUsername) {
        log.info("Initializing Account creation request");
        //filling the primitive fields in account first
        Account account= AccountMapper.mapDtoToModel(accountOpenReqDto);
        //fetching the customer object of logged in customer
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        //validating and fetching the branch using IFSC Code
        Branch branch= Optional.ofNullable(branchService.getBranchByIfscCode(accountOpenReqDto.ifscCode()))
                .orElseThrow(()->new ResourceNotFoundException("Branch IFSC Code is invalid"));
        //Setting the foreign objects
        account.setTransactionPin(passwordEncoder.encode(accountOpenReqDto.transactionPin()));
        account.setRequestStatus(RequestStatus.OPEN);
        account.setBranch(branch);
        account.setCustomer(customer);
        //inserting Account
        accountRepository.save(account);
        log.info("Account is created but yet to be active");
    }

    public AccountFilterDto dynamicFilterAccount(AccountType accountType, BigDecimal minBalance, BigDecimal maxBalance,
                                                 Boolean isActive, RequestStatus requestStatus, LocalDate createdFrom,
                                                 LocalDate createdTill, LocalDate activatedFrom, LocalDate activatedTill,
                                                 String ifscCode, Long customerId, Long accountId, LocalDate requestedFrom,
                                                 LocalDate requestedTill, Integer page, Integer size) {
        log.info("Searching Accounts through Dynamic Filter");
        //Creating Pageable object for Pagination
        Pageable pageable= PageRequest.of(page,size);
        log.info("Returning Filtered Accounts");
        //filtering the accounts in the repository and returning them
        Page<AccountRespDto> pageDto = accountRepository.dynamicFilterAccount(accountType, minBalance, maxBalance,
                isActive, requestStatus, createdFrom,
                createdTill, activatedFrom, activatedTill,
                ifscCode, customerId, accountId, requestedFrom, requestedTill, pageable);
        return new AccountFilterDto(pageDto.getContent(),
                new PaginationDto(pageDto.getTotalPages(),pageDto.getTotalElements(), pageDto.getNumberOfElements()));
    }

    public AccountStatusRespDto handleAccount(Long accountId, Boolean activate, String employeeUsername) {
        log.info("The Clerk starts handling Account request");
        // getting the current logged in employee
        Employee employee = employeeRepository.getEmployeeByUsername(employeeUsername);
        //getting the account that made request if not throw exception
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account Id is Invalid"));
        // check if the account and employee belong to the same branch if not throw exception
        if(!(employee.getBranch().getId().equals(account.getBranch().getId()))){
            throw new OutOfReachException("Account and Employee are not from the same Branch");
        }
        if(activate.equals(account.isActive())){
            throw new UnchangedStateException("The Request doesn't change Account state");
        }
        //if activate is true , account is activated else deactivated
        if(activate){
            account.setActive(activate);
            account.setActivatedAt(LocalDate.now());
        }
        else{
            account.setActive(activate);
        }
        //all the request status field is set default
        account.setRequestStatus(RequestStatus.NONE);
        account.setRequestReason("None");
        // the verifying Employee is added to account
        account.setEmployee(employee);
        // Account changes are saved
        accountRepository.save(account);
        log.info("The Account request was Full filled");
        // returning updated Account status
        return accountRepository.getUpdatedAccountStatus(accountId);
    }

    public AccountFilterDto dynamicFilterCustomerAccount(AccountType accountType, BigDecimal minBalance, BigDecimal maxBalance,
                                                             Boolean isActive, RequestStatus requestStatus, LocalDate createdFrom,
                                                             LocalDate createdTill, LocalDate activatedFrom, LocalDate activatedTill,
                                                             String ifscCode, Long accountId, LocalDate requestedFrom,
                                                             LocalDate requestedTill, Integer page, Integer size, String customerUsername) {
        log.info("Searching Customer Accounts through Dynamic Filter");
        //getting Customer, to get ID and only search their accounts
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        //Creating Pageable object for Pagination
        Pageable pageable= PageRequest.of(page,size);
        log.info("Returning Filtered Customer Accounts");
        //filtering the accounts in the repository and returning them
        Page<AccountRespDto> pageDto = accountRepository.dynamicFilterAccount(accountType, minBalance, maxBalance,
                isActive, requestStatus, createdFrom,
                createdTill, activatedFrom, activatedTill,
                ifscCode, customer.getId(), accountId, requestedFrom, requestedTill, pageable);
        return new AccountFilterDto(pageDto.getContent(),
                new PaginationDto(pageDto.getTotalPages(),pageDto.getTotalElements(), pageDto.getNumberOfElements()));
    }
    @Transactional
    public AccountRespDto closeAccount(AccountCloseReqDto accountCloseReqDto, String customerUsername) {
        log.info("Initiating Account Closure Request");
        //Getting the logged in customer
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        //Checking if the ID is valid else throw Exception
        Account account = accountRepository.findById(accountCloseReqDto.accountId())
                .orElseThrow(()-> new ResourceNotFoundException("Account ID is Invalid"));
        //Checking if the Account is Active to close else Throw Exception
        if(!(account.isActive())){
            throw new OutOfReachException("Account is not accessible as it is Inactive");
        }
        //Checking if the account belongs to the Customer else throw Exception
        if(!(account.getCustomer().getId().equals(customer.getId()))){
            throw new OwnershipException("The Account does not belong to Customer");
        }
        //Checking if the current Account Request status is not CLOSe else throw Exception
        if(account.getRequestStatus().equals(RequestStatus.CLOSE)){
            throw new UnchangedStateException("This Account has already requested Close");
        }
        //Updating the required Account fields for the Account CLOSE request and saving it
        account.setRequestStatus(RequestStatus.CLOSE);
        account.setRequestReason(accountCloseReqDto.requestReason());
        account.setRequestedAt(LocalDate.now());
        accountRepository.save(account);
        log.info("Account CLOSE request was successfully created");
        //returns the updates Account info
        return accountRepository.getAccountInfoById(account.getId());
    }

    public ChartDto getAccountRequestStatusStats(Principal principal) {
        Employee employee = employeeRepository.findByUserUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        List<AccountRequestStatusStatDto> list =
                accountRepository.getAccountRequestStatusStats(employee.getBranch().getId());

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        list.forEach(e -> {
            labels.add(e.requestStatus().toString());
            values.add(e.numberOfAccounts());
        });
        return new ChartDto(labels, values);
    }


}
