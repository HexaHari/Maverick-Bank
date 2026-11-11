package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.DepositReqDto;
import com.bank.MaverickBank.dto.request.TransferReqDto;
import com.bank.MaverickBank.dto.request.WithdrawReqDto;
import com.bank.MaverickBank.dto.response.StatementDto;
import com.bank.MaverickBank.dto.response.TransactionFilterDto;
import com.bank.MaverickBank.dto.response.TransactionRespDto;
import com.bank.MaverickBank.dto.response.TransferRespDto;
import com.bank.MaverickBank.dto.response.WithdrawRespDto;
import com.bank.MaverickBank.enums.AccountType;
import com.bank.MaverickBank.enums.TransactionType;
import com.bank.MaverickBank.exception.IncorrectPinException;
import com.bank.MaverickBank.exception.LimitExceedException;
import com.bank.MaverickBank.exception.OutOfReachException;
import com.bank.MaverickBank.exception.OwnershipException;
import com.bank.MaverickBank.exception.ResourceNotFoundException;
import com.bank.MaverickBank.exception.UnchangedStateException;
import com.bank.MaverickBank.model.Account;
import com.bank.MaverickBank.model.Branch;
import com.bank.MaverickBank.model.Customer;
import com.bank.MaverickBank.model.Transaction;
import com.bank.MaverickBank.repository.AccountRepository;
import com.bank.MaverickBank.repository.CustomerRepository;
import com.bank.MaverickBank.repository.TransactionRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
   Note: depositAmount saves the cheque image in to a fixed folder on the disk (uploadDir),
   so only the cases that stop before saving the image are tested here.
 */
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @InjectMocks
    private TransactionService transactionService;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MultipartFile chequeImage;

    private Customer createCustomer(Long id,String name){
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setAddress("Vellore");
        return customer;
    }

    private Account createAccount(Long id,boolean active,Customer owner,String balance){
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setBranchName("Chennai Main");
        branch.setIfscCode("MAVB0000301");
        Account account = new Account();
        account.setId(id);
        account.setAccountType(AccountType.SAVINGS);
        account.setActive(active);
        account.setCustomer(owner);
        account.setBranch(branch);
        account.setBalance(new BigDecimal(balance));
        account.setTransactionPin("encodedPin");
        return account;
    }

    private TransactionRespDto createTransactionResp(Long id,Long sourceAccountId,Long sourceCustomerId,String sourceCustomerName,
                                                     Long destinationAccountId,Long destinationCustomerId,String destinationCustomerName,
                                                     String amount,TransactionType type,String sourceBalance,String destinationBalance,
                                                     String completedAt){
        return new TransactionRespDto(id,sourceAccountId,sourceCustomerId,sourceCustomerName,destinationAccountId,
                destinationCustomerId,destinationCustomerName,new BigDecimal(amount),type,
                sourceBalance == null ? null : new BigDecimal(sourceBalance),
                destinationBalance == null ? null : new BigDecimal(destinationBalance),
                true,Instant.parse(completedAt));
    }


    // ---------------------------------------------------- depositAmount

    @Test
    public void depositAmountTest_InvalidAccount(){
        DepositReqDto dto = new DepositReqDto(99L,new BigDecimal("1000"));

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(createCustomer(7L,"Monisha"));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->transactionService.depositAmount(dto,chequeImage,"monisha"));
        assertEquals("Account Id is Invalid",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(99L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void depositAmountTest_InactiveAccount(){
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(11L,false,customer,"5000.00");
        DepositReqDto dto = new DepositReqDto(11L,new BigDecimal("1000"));

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        OutOfReachException exception = assertThrows(OutOfReachException.class,
                ()->transactionService.depositAmount(dto,chequeImage,"monisha"));
        assertEquals("Account is not accessible as it is Inactive",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void depositAmountTest_NotOwner(){
        Account account = createAccount(11L,true,createCustomer(8L,"Ravi"),"5000.00");
        DepositReqDto dto = new DepositReqDto(11L,new BigDecimal("1000"));

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(createCustomer(7L,"Monisha"));
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        OwnershipException exception = assertThrows(OwnershipException.class,
                ()->transactionService.depositAmount(dto,chequeImage,"monisha"));
        assertEquals("The Account does not belong to Customer",exception.getMessage());
        assertEquals(new BigDecimal("5000.00"),account.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void depositAmountTest_LimitExceeded(){
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(11L,true,customer,"5000.00");
        DepositReqDto dto = new DepositReqDto(11L,new BigDecimal("50000.01"));

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        //Rs.50000 is the most that can be deposited at a time
        LimitExceedException exception = assertThrows(LimitExceedException.class,
                ()->transactionService.depositAmount(dto,chequeImage,"monisha"));
        assertEquals("You Can't Deposit more than Rs.50000 at a single Instance",exception.getMessage());
        assertEquals(new BigDecimal("5000.00"),account.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(0)).save(any(Account.class));
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void depositAmountTest_NoChequeImage(){
        //exactly Rs.50000 is within the limit, so it goes on and stops at the missing cheque image
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(11L,true,customer,"5000.00");
        DepositReqDto dto = new DepositReqDto(11L,new BigDecimal("50000"));

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(chequeImage.getOriginalFilename()).thenReturn("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->transactionService.depositAmount(dto,chequeImage,"monisha"));
        assertEquals("No Valid Check Image was given",exception.getMessage());
        assertEquals(new BigDecimal("5000.00"),account.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(0)).save(any(Account.class));
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    // ---------------------------------------------------- withdrawAmount

    @Test
    public void withdrawAmountTest(){
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(11L,true,customer,"5000.00");
        WithdrawReqDto dto = new WithdrawReqDto(11L,new BigDecimal("1000.00"),"1234");
        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(5L);
        WithdrawRespDto respDto = new WithdrawRespDto(5L,11L,new BigDecimal("1000.00"),new BigDecimal("4000.00"),
                TransactionType.WITHDRAW,true,Instant.parse("2026-11-10T10:00:00Z"));

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("1234","encodedPin")).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionRepository.getWithdrawTransactionResp(5L)).thenReturn(respDto);

        WithdrawRespDto result = transactionService.withdrawAmount(dto,"monisha");

        assertEquals(respDto,result);
        assertEquals(new BigDecimal("4000.00"),account.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(passwordEncoder,times(1)).matches("1234","encodedPin");
        verify(accountRepository,times(1)).save(account);
        verify(transactionRepository,times(1)).save(any(Transaction.class));
        verify(transactionRepository,times(1)).getWithdrawTransactionResp(5L);

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void withdrawAmountTest_WholeBalance(){
        //the whole balance can be withdrawn, only more than the balance is not allowed
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(11L,true,customer,"5000.00");
        WithdrawReqDto dto = new WithdrawReqDto(11L,new BigDecimal("5000.00"),"1234");
        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(5L);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("1234","encodedPin")).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        transactionService.withdrawAmount(dto,"monisha");

        assertEquals(new BigDecimal("0.00"),account.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(passwordEncoder,times(1)).matches("1234","encodedPin");
        verify(accountRepository,times(1)).save(account);
        verify(transactionRepository,times(1)).save(any(Transaction.class));
        verify(transactionRepository,times(1)).getWithdrawTransactionResp(5L);

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void withdrawAmountTest_InvalidAccount(){
        WithdrawReqDto dto = new WithdrawReqDto(99L,new BigDecimal("1000"),"1234");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(createCustomer(7L,"Monisha"));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->transactionService.withdrawAmount(dto,"monisha"));
        assertEquals("Account Id is Invalid",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(99L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void withdrawAmountTest_InactiveAccount(){
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(11L,false,customer,"5000.00");
        WithdrawReqDto dto = new WithdrawReqDto(11L,new BigDecimal("1000"),"1234");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        assertThrows(OutOfReachException.class,()->transactionService.withdrawAmount(dto,"monisha"));
        assertEquals(new BigDecimal("5000.00"),account.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void withdrawAmountTest_NotOwner(){
        Account account = createAccount(11L,true,createCustomer(8L,"Ravi"),"5000.00");
        WithdrawReqDto dto = new WithdrawReqDto(11L,new BigDecimal("1000"),"1234");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(createCustomer(7L,"Monisha"));
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        assertThrows(OwnershipException.class,()->transactionService.withdrawAmount(dto,"monisha"));
        assertEquals(new BigDecimal("5000.00"),account.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void withdrawAmountTest_NotEnoughBalance(){
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(11L,true,customer,"5000.00");
        WithdrawReqDto dto = new WithdrawReqDto(11L,new BigDecimal("5000.01"),"1234");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        LimitExceedException exception = assertThrows(LimitExceedException.class,
                ()->transactionService.withdrawAmount(dto,"monisha"));
        assertEquals("The Requested amount exceeds the Account balance",exception.getMessage());
        assertEquals(new BigDecimal("5000.00"),account.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(passwordEncoder,times(0)).matches("1234","encodedPin");
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void withdrawAmountTest_WrongPin(){
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(11L,true,customer,"5000.00");
        WithdrawReqDto dto = new WithdrawReqDto(11L,new BigDecimal("1000"),"9999");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("9999","encodedPin")).thenReturn(false);

        IncorrectPinException exception = assertThrows(IncorrectPinException.class,
                ()->transactionService.withdrawAmount(dto,"monisha"));
        assertEquals("The Transaction Pin doesn't Match",exception.getMessage());
        //the money stays in the account when the pin is wrong
        assertEquals(new BigDecimal("5000.00"),account.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(passwordEncoder,times(1)).matches("9999","encodedPin");
        verify(accountRepository,times(0)).save(any(Account.class));
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    // ---------------------------------------------------- transferAmount

    @Test
    public void transferAmountTest(){
        Customer customer = createCustomer(7L,"Monisha");
        Account source = createAccount(11L,true,customer,"5000.00");
        Account destination = createAccount(12L,true,createCustomer(8L,"Ravi"),"1000.00");
        TransferReqDto dto = new TransferReqDto(11L,12L,new BigDecimal("1000.00"),"1234");
        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(9L);
        TransferRespDto respDto = new TransferRespDto(9L,11L,12L,new BigDecimal("1000.00"),new BigDecimal("4000.00"),
                TransactionType.TRANSFER,true,Instant.parse("2026-11-10T10:00:00Z"));

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(12L)).thenReturn(Optional.of(destination));
        when(passwordEncoder.matches("1234","encodedPin")).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionRepository.getTransferTransactionResp(9L)).thenReturn(respDto);

        TransferRespDto result = transactionService.transferAmount(dto,"monisha");

        assertEquals(respDto,result);
        //money leaves the source account and reaches the destination account
        assertEquals(new BigDecimal("4000.00"),source.getBalance());
        assertEquals(new BigDecimal("2000.00"),destination.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(1)).findById(12L);
        verify(passwordEncoder,times(1)).matches("1234","encodedPin");
        verify(accountRepository,times(1)).save(source);
        verify(accountRepository,times(1)).save(destination);
        verify(transactionRepository,times(1)).save(any(Transaction.class));
        verify(transactionRepository,times(1)).getTransferTransactionResp(9L);

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void transferAmountTest_SameAccount(){
        TransferReqDto dto = new TransferReqDto(11L,11L,new BigDecimal("1000"),"1234");

        UnchangedStateException exception = assertThrows(UnchangedStateException.class,
                ()->transactionService.transferAmount(dto,"monisha"));
        assertEquals("Both Source and Destination Account ID needs to be different for transfer",exception.getMessage());

        //nothing is read or saved
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void transferAmountTest_InvalidSourceAccount(){
        TransferReqDto dto = new TransferReqDto(99L,12L,new BigDecimal("1000"),"1234");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(createCustomer(7L,"Monisha"));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->transactionService.transferAmount(dto,"monisha"));
        assertEquals("Sender Account Id is Invalid",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(99L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void transferAmountTest_InvalidDestinationAccount(){
        Customer customer = createCustomer(7L,"Monisha");
        Account source = createAccount(11L,true,customer,"5000.00");
        TransferReqDto dto = new TransferReqDto(11L,99L,new BigDecimal("1000"),"1234");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->transactionService.transferAmount(dto,"monisha"));
        assertEquals("Receiver Account Id is Invalid",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(1)).findById(99L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void transferAmountTest_InactiveSourceAccount(){
        Customer customer = createCustomer(7L,"Monisha");
        Account source = createAccount(11L,false,customer,"5000.00");
        Account destination = createAccount(12L,true,createCustomer(8L,"Ravi"),"1000.00");
        TransferReqDto dto = new TransferReqDto(11L,12L,new BigDecimal("1000"),"1234");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(12L)).thenReturn(Optional.of(destination));

        OutOfReachException exception = assertThrows(OutOfReachException.class,
                ()->transactionService.transferAmount(dto,"monisha"));
        assertEquals("Sender Account is not accessible as it is Inactive",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(1)).findById(12L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void transferAmountTest_InactiveDestinationAccount(){
        Customer customer = createCustomer(7L,"Monisha");
        Account source = createAccount(11L,true,customer,"5000.00");
        Account destination = createAccount(12L,false,createCustomer(8L,"Ravi"),"1000.00");
        TransferReqDto dto = new TransferReqDto(11L,12L,new BigDecimal("1000"),"1234");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(12L)).thenReturn(Optional.of(destination));

        OutOfReachException exception = assertThrows(OutOfReachException.class,
                ()->transactionService.transferAmount(dto,"monisha"));
        assertEquals("Receiver Account is not accessible as it is Inactive",exception.getMessage());
        assertEquals(new BigDecimal("5000.00"),source.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(1)).findById(12L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void transferAmountTest_SourceNotOwner(){
        //monisha can not send money out of the account of another customer
        Account source = createAccount(11L,true,createCustomer(8L,"Ravi"),"5000.00");
        Account destination = createAccount(12L,true,createCustomer(9L,"Giri"),"1000.00");
        TransferReqDto dto = new TransferReqDto(11L,12L,new BigDecimal("1000"),"1234");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(createCustomer(7L,"Monisha"));
        when(accountRepository.findById(11L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(12L)).thenReturn(Optional.of(destination));

        assertThrows(OwnershipException.class,()->transactionService.transferAmount(dto,"monisha"));
        assertEquals(new BigDecimal("5000.00"),source.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(1)).findById(12L);
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void transferAmountTest_NotEnoughBalance(){
        Customer customer = createCustomer(7L,"Monisha");
        Account source = createAccount(11L,true,customer,"5000.00");
        Account destination = createAccount(12L,true,createCustomer(8L,"Ravi"),"1000.00");
        TransferReqDto dto = new TransferReqDto(11L,12L,new BigDecimal("5000.01"),"1234");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(12L)).thenReturn(Optional.of(destination));

        assertThrows(LimitExceedException.class,()->transactionService.transferAmount(dto,"monisha"));
        assertEquals(new BigDecimal("5000.00"),source.getBalance());
        assertEquals(new BigDecimal("1000.00"),destination.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(1)).findById(12L);
        verify(passwordEncoder,times(0)).matches("1234","encodedPin");
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void transferAmountTest_WrongPin(){
        Customer customer = createCustomer(7L,"Monisha");
        Account source = createAccount(11L,true,customer,"5000.00");
        Account destination = createAccount(12L,true,createCustomer(8L,"Ravi"),"1000.00");
        TransferReqDto dto = new TransferReqDto(11L,12L,new BigDecimal("1000"),"9999");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(12L)).thenReturn(Optional.of(destination));
        when(passwordEncoder.matches("9999","encodedPin")).thenReturn(false);

        assertThrows(IncorrectPinException.class,()->transactionService.transferAmount(dto,"monisha"));
        assertEquals(new BigDecimal("5000.00"),source.getBalance());
        assertEquals(new BigDecimal("1000.00"),destination.getBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(accountRepository,times(1)).findById(12L);
        verify(passwordEncoder,times(1)).matches("9999","encodedPin");
        verify(accountRepository,times(0)).save(any(Account.class));
        verify(transactionRepository,times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    // ---------------------------------------------------- filters

    @Test
    public void dynamicFilterTransactionTest(){
        TransactionRespDto dto1 = createTransactionResp(1L,11L,7L,"Monisha",12L,8L,"Ravi","1000.00",TransactionType.TRANSFER,
                "4000.00","2000.00","2026-11-10T10:00:00Z");
        TransactionRespDto dto2 = createTransactionResp(2L,11L,7L,"Monisha",13L,9L,"Giri","500.00",TransactionType.TRANSFER,
                "3500.00","1500.00","2026-11-11T10:00:00Z");
        BigDecimal minAmount = new BigDecimal("100");
        LocalDate completedFrom = LocalDate.of(2026,11,1);
        LocalDate completedTill = LocalDate.of(2026,11,30);

        //the dates are changed to the start of the day in UTC, and till date includes the whole last day
        when(transactionRepository.dynamicFilterTransaction(null,11L,null,TransactionType.TRANSFER,minAmount,null,true,
                Instant.parse("2026-11-01T00:00:00Z"),Instant.parse("2026-12-01T00:00:00Z"),null,null,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(dto1,dto2),PageRequest.of(0,5),2));

        TransactionFilterDto result = transactionService.dynamicFilterTransaction(null,11L,null,TransactionType.TRANSFER,minAmount,
                null,true,completedFrom,completedTill,null,null,0,5);

        assertEquals(2,result.transactionList().size());
        assertEquals(1L,result.transactionList().get(0).transactionId());
        assertEquals(1,result.paginationInfo().totalPages());
        assertEquals(2L,result.paginationInfo().totalRecords());
        assertEquals(2,result.paginationInfo().numRecordsInPage());

        verify(transactionRepository,times(1)).dynamicFilterTransaction(null,11L,null,TransactionType.TRANSFER,minAmount,null,true,
                Instant.parse("2026-11-01T00:00:00Z"),Instant.parse("2026-12-01T00:00:00Z"),null,null,PageRequest.of(0,5));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void dynamicFilterTransactionTest_NoDates(){
        TransactionRespDto dto1 = createTransactionResp(1L,11L,7L,"Monisha",12L,8L,"Ravi","1000.00",TransactionType.TRANSFER,
                "4000.00","2000.00","2026-11-10T10:00:00Z");

        when(transactionRepository.dynamicFilterTransaction(null,null,null,null,null,null,null,null,null,null,null,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(dto1),PageRequest.of(0,5),1));

        TransactionFilterDto result = transactionService.dynamicFilterTransaction(null,null,null,null,null,null,null,
                null,null,null,null,0,5);

        assertEquals(1,result.transactionList().size());

        verify(transactionRepository,times(1)).dynamicFilterTransaction(null,null,null,null,null,null,null,null,null,null,null,PageRequest.of(0,5));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void dynamicFilterTransactionTest_Invalid(){
        //page number can not be negative, so the repository is never reached
        assertThrows(IllegalArgumentException.class,
                ()->transactionService.dynamicFilterTransaction(null,null,null,null,null,null,null,null,null,null,null,-1,5));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void dynamicCustomerFilterTransactionTest(){
        Customer customer = createCustomer(7L,"Monisha");
        //monisha sent money to ravi, and giri sent money to monisha
        TransactionRespDto sent = createTransactionResp(1L,11L,7L,"Monisha",12L,8L,"Ravi","1000.00",TransactionType.TRANSFER,
                "4000.00","2000.00","2026-11-10T10:00:00Z");
        TransactionRespDto received = createTransactionResp(2L,13L,9L,"Giri",11L,7L,"Monisha","300.00",TransactionType.TRANSFER,
                "700.00","4300.00","2026-11-11T10:00:00Z");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        //the logged in customer id is used for both source and destination customer, so only own transactions come
        when(transactionRepository.dynamicFilterTransaction(null,null,null,null,null,null,true,null,null,7L,7L,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(sent,received),PageRequest.of(0,5),2));

        TransactionFilterDto result = transactionService.dynamicCustomerFilterTransaction(null,null,null,null,null,null,true,
                null,null,0,5,"monisha");

        assertEquals(2,result.transactionList().size());
        //monisha can see only her own account balance, never the balance of the other customer
        TransactionRespDto first = result.transactionList().get(0);
        assertEquals(new BigDecimal("4000.00"),first.sourceAccountBalance());
        assertNull(first.destinationAccountBalance());
        TransactionRespDto second = result.transactionList().get(1);
        assertNull(second.sourceAccountBalance());
        assertEquals(new BigDecimal("4300.00"),second.destinationAccountBalance());
        assertEquals(1,result.paginationInfo().totalPages());
        assertEquals(2L,result.paginationInfo().totalRecords());
        assertEquals(2,result.paginationInfo().numRecordsInPage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(transactionRepository,times(1)).dynamicFilterTransaction(null,null,null,null,null,null,true,null,null,7L,7L,PageRequest.of(0,5));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void dynamicCustomerFilterTransactionTest_Invalid(){
        //page size can not be zero, so nothing is read
        assertThrows(IllegalArgumentException.class,
                ()->transactionService.dynamicCustomerFilterTransaction(null,null,null,null,null,null,null,null,null,0,0,"monisha"));

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    // ---------------------------------------------------- getStatement

    @Test
    public void getStatementTest(){
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(11L,true,customer,"5800.00");
        LocalDate fromDate = LocalDate.of(2026,11,1);
        LocalDate tillDate = LocalDate.of(2026,11,30);

        //before the period: the balance after this deposit is the opening balance
        TransactionRespDto before = createTransactionResp(1L,null,null,null,11L,7L,"Monisha","5000.00",TransactionType.DEPOSIT,
                null,"5000.00","2026-10-20T10:00:00Z");
        TransactionRespDto deposit = createTransactionResp(2L,null,null,null,11L,7L,"Monisha","2000.00",TransactionType.DEPOSIT,
                null,"7000.00","2026-11-03T10:00:00Z");
        TransactionRespDto withdraw = createTransactionResp(3L,11L,7L,"Monisha",null,null,null,"500.00",TransactionType.WITHDRAW,
                "6500.00",null,"2026-11-05T10:00:00Z");
        TransactionRespDto transferOut = createTransactionResp(4L,11L,7L,"Monisha",12L,8L,"Ravi","1000.00",TransactionType.TRANSFER,
                "5500.00","2000.00","2026-11-10T10:00:00Z");
        TransactionRespDto transferIn = createTransactionResp(5L,13L,9L,"Giri",11L,7L,"Monisha","300.00",TransactionType.TRANSFER,
                "700.00","5800.00","2026-11-15T10:00:00Z");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        //the transactions come in a mixed order, the statement must put them in the order of time
        when(transactionRepository.dynamicFilterTransaction(null,11L,11L,null,null,null,true,null,
                Instant.parse("2026-12-01T00:00:00Z"),7L,7L,Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(transferOut,before,transferIn,withdraw,deposit)));

        StatementDto result = transactionService.getStatement(11L,fromDate,tillDate,"monisha");

        assertEquals("Monisha",result.accountHolderName());
        assertEquals("SAVINGS",result.accountType());
        assertEquals(11L,result.accountId());
        assertEquals("Vellore",result.address());
        assertEquals("Chennai Main",result.branchName());
        assertEquals("MAVB0000301",result.ifscCode());
        assertEquals("INR",result.currency());
        assertEquals(fromDate,result.fromDate());
        assertEquals(tillDate,result.toDate());
        assertEquals(new BigDecimal("5000.00"),result.openingBalance());
        assertEquals(new BigDecimal("2300.00"),result.totalCredits());
        assertEquals(new BigDecimal("1500.00"),result.totalDebits());
        assertEquals(new BigDecimal("5800.00"),result.endingBalance());

        //the transaction before the period is not a row
        assertEquals(4,result.activity().size());

        assertEquals(LocalDate.of(2026,11,3),result.activity().get(0).date());
        assertEquals("Cheque Deposit",result.activity().get(0).details());
        assertNull(result.activity().get(0).debit());
        assertEquals(new BigDecimal("2000.00"),result.activity().get(0).credit());
        assertEquals(new BigDecimal("7000.00"),result.activity().get(0).balance());

        assertEquals(LocalDate.of(2026,11,5),result.activity().get(1).date());
        assertEquals("Cash Withdrawal",result.activity().get(1).details());
        assertEquals(new BigDecimal("500.00"),result.activity().get(1).debit());
        assertNull(result.activity().get(1).credit());
        assertEquals(new BigDecimal("6500.00"),result.activity().get(1).balance());

        assertEquals("Transfer to 12 - Ravi",result.activity().get(2).details());
        assertEquals(new BigDecimal("1000.00"),result.activity().get(2).debit());
        assertNull(result.activity().get(2).credit());
        assertEquals(new BigDecimal("5500.00"),result.activity().get(2).balance());

        assertEquals("Transfer from 13 - Giri",result.activity().get(3).details());
        assertNull(result.activity().get(3).debit());
        assertEquals(new BigDecimal("300.00"),result.activity().get(3).credit());
        assertEquals(new BigDecimal("5800.00"),result.activity().get(3).balance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(transactionRepository,times(1)).dynamicFilterTransaction(null,11L,11L,null,null,null,true,null,
                Instant.parse("2026-12-01T00:00:00Z"),7L,7L,Pageable.unpaged());

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void getStatementTest_NothingInPeriod(){
        //when nothing happened in the period the ending balance is the same as the opening balance
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(11L,true,customer,"5000.00");
        TransactionRespDto before = createTransactionResp(1L,null,null,null,11L,7L,"Monisha","5000.00",TransactionType.DEPOSIT,
                null,"5000.00","2026-10-20T10:00:00Z");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(transactionRepository.dynamicFilterTransaction(null,11L,11L,null,null,null,true,null,
                Instant.parse("2026-12-01T00:00:00Z"),7L,7L,Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(before)));

        StatementDto result = transactionService.getStatement(11L,LocalDate.of(2026,11,1),LocalDate.of(2026,11,30),"monisha");

        assertTrue(result.activity().isEmpty());
        assertEquals(new BigDecimal("5000.00"),result.openingBalance());
        assertEquals(BigDecimal.ZERO,result.totalCredits());
        assertEquals(BigDecimal.ZERO,result.totalDebits());
        assertEquals(new BigDecimal("5000.00"),result.endingBalance());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(transactionRepository,times(1)).dynamicFilterTransaction(null,11L,11L,null,null,null,true,null,
                Instant.parse("2026-12-01T00:00:00Z"),7L,7L,Pageable.unpaged());

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void getStatementTest_InvalidAccount(){
        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(createCustomer(7L,"Monisha"));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->transactionService.getStatement(99L,LocalDate.of(2026,11,1),LocalDate.of(2026,11,30),"monisha"));
        assertEquals("Account Id is Invalid",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(99L);

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void getStatementTest_NotOwner(){
        //monisha can not download the statement of another customer's account
        Account account = createAccount(11L,true,createCustomer(8L,"Ravi"),"5000.00");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(createCustomer(7L,"Monisha"));
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        OwnershipException exception = assertThrows(OwnershipException.class,
                ()->transactionService.getStatement(11L,LocalDate.of(2026,11,1),LocalDate.of(2026,11,30),"monisha"));
        assertEquals("The Account does not belong to Customer",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(11L);
        verify(transactionRepository,times(0)).dynamicFilterTransaction(any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any());

        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }
}
