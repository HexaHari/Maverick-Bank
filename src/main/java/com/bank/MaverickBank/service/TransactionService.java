package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.DepositReqDto;
import com.bank.MaverickBank.dto.request.TransferReqDto;
import com.bank.MaverickBank.dto.request.WithdrawReqDto;
import com.bank.MaverickBank.dto.response.*;
import com.bank.MaverickBank.enums.TransactionType;
import com.bank.MaverickBank.exception.*;
import com.bank.MaverickBank.model.Account;
import com.bank.MaverickBank.model.Customer;
import com.bank.MaverickBank.model.Transaction;
import com.bank.MaverickBank.repository.AccountRepository;
import com.bank.MaverickBank.repository.CustomerRepository;
import com.bank.MaverickBank.repository.TransactionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final static String uploadDir ="C:/Users/srih8/NodeJavaScriptFsd/MavericBank-ui/public/uploads/";

    @Transactional
    public DepositRespDto depositAmount(@Valid DepositReqDto depositReqDto, MultipartFile file, String customerUsername) throws IOException {
        log.info("Initiating Deposit Transaction for the give Account");
        //Getting the logged in Customer
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        //Checking if the given Account ID is valid else throw exception
        Account account = accountRepository.findById(depositReqDto.destinationAccountId())
                .orElseThrow(()-> new ResourceNotFoundException("Account Id is Invalid"));
        //Checking if the given Account ID is for an active account else Throw Exception
        if(!(account.isActive())){
            throw new OutOfReachException("Account is not accessible as it is Inactive");
        }
        //Checking if the given Account ID belongs to Customer else Throw Exception
        if(!(account.getCustomer().getId().equals(customer.getId()))){
            throw new OwnershipException("The Account does not belong to Customer");
        }
        //Checking to See if the Deposit is within the limit 0f Rs.50000 else Throw Exception
        if (depositReqDto.amount().compareTo(new BigDecimal("50000")) > 0){
            throw new LimitExceedException("You Can't Deposit more than Rs.50000 at a single Instance");
        }
        if(file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
            throw new ResourceNotFoundException("No Valid Check Image was given");
        log.info("Uploaded File Name: {}", file.getOriginalFilename());
        log.info("File Size: {}", file.getSize());
        String ext = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];
        log.info("File Extension: {}", ext);
        String originalFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), Paths.get(uploadDir+originalFileName), StandardCopyOption.REPLACE_EXISTING);
        //Adding the deposit to the balance and saving it in the account
        BigDecimal newBalance = account.getBalance().add(depositReqDto.amount());
        account.setBalance(newBalance);
        accountRepository.save(account);
        //Creating a Transaction Object recording the Deposit info and Saving it
        Transaction transaction = new Transaction();
        transaction.setDestinationAccount(account);
        transaction.setAmount(depositReqDto.amount());
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setDestinationAccountBalance(account.getBalance());
        transaction.setChequeImage(originalFileName);
        transaction.setCompleted(true);
        transaction=transactionRepository.save(transaction);
        //Returning the Deposit Transaction info
        log.info("Deposit transaction is complete and its info is returned");
        return transactionRepository.getDepositTransactionResp(transaction.getId());
    }

    @Transactional
    public WithdrawRespDto withdrawAmount(@Valid WithdrawReqDto withdrawReqDto, String customerUsername) {
        log.info("Initiating Withdraw Transaction for the given Account");
        //Getting the logged in Customer
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        //Checking if the given Account ID is valid else throw exception
        Account account = accountRepository.findById(withdrawReqDto.sourceAccountId())
                .orElseThrow(()-> new ResourceNotFoundException("Account Id is Invalid"));
        //Checking if the given Account ID is for an active account else Throw Exception
        if(!(account.isActive())){
            throw new OutOfReachException("Account is not accessible as it is Inactive");
        }
        //Checking if the given Account ID belongs to Customer else Throw Exception
        if(!(account.getCustomer().getId().equals(customer.getId()))){
            throw new OwnershipException("The Account does not belong to Customer");
        }
        //Checking to see if the requested withdrawal amount is within Account balance else throw Exception
        if(withdrawReqDto.amount().compareTo(account.getBalance()) > 0){
            throw new LimitExceedException("The Requested amount exceeds the Account balance");
        }
        //Checking to see if the Transaction Pin matches the account to verify Authentication once more
        if(!(passwordEncoder.matches(withdrawReqDto.transactionPin(), account.getTransactionPin()))){
            throw new IncorrectPinException("The Transaction Pin doesn't Match");
        }
        //Subtracting the Withdraw amount from the balance and saving the account
        BigDecimal newBalance = account.getBalance().subtract(withdrawReqDto.amount());
        account.setBalance(newBalance);
        accountRepository.save(account);
        //Creating a Transaction Object recording the Withdraw info and Saving it
        Transaction transaction = new Transaction();
        transaction.setSourceAccount(account);
        transaction.setAmount(withdrawReqDto.amount());
        transaction.setTransactionType(TransactionType.WITHDRAW);
        transaction.setSourceAccountBalance(account.getBalance());
        transaction.setCompleted(true);
        transaction=transactionRepository.save(transaction);
        //Returning the Withdraw Transaction info
        log.info("Withdraw transaction is complete and its info is returned");
        return transactionRepository.getWithdrawTransactionResp(transaction.getId());
    }

    @Transactional
    public TransferRespDto transferAmount(@Valid TransferReqDto transferReqDto, String customerUsername) {
        log.info("Initiating Transfer Transaction for the given Account");
        //Checking if Source and Destination Account IDs are different if not throw Exception
        if(transferReqDto.sourceAccountId().equals(transferReqDto.destinationAccountId())){
            throw new UnchangedStateException("Both Source and Destination Account ID needs to be different for transfer");
        }
        //Getting the logged in Customer
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        //Checking if the given Source and Destination Account IDs are valid else throw exception
        Account sourceAccount = accountRepository.findById(transferReqDto.sourceAccountId())
                .orElseThrow(()-> new ResourceNotFoundException("Sender Account Id is Invalid"));
        Account destinationAccount = accountRepository.findById(transferReqDto.destinationAccountId())
                .orElseThrow(()-> new ResourceNotFoundException("Receiver Account Id is Invalid"));
        //Checking if the given Source Account ID is for an active account else Throw Exception
        if(!(sourceAccount.isActive())){
            throw new OutOfReachException("Sender Account is not accessible as it is Inactive");
        }
        //Checking if the given Destination Account ID is for an active account else Throw Exception
        if(!(destinationAccount.isActive())){
            throw new OutOfReachException("Receiver Account is not accessible as it is Inactive");
        }
        //Checking if the given Source Account ID belongs to Customer else Throw Exception
        if(!(sourceAccount.getCustomer().getId().equals(customer.getId()))){
            throw new OwnershipException("The Account does not belong to Customer");
        }
        //Checking to see if the requested transfer amount is within Source Account balance else throw Exception
        if(transferReqDto.amount().compareTo(sourceAccount.getBalance()) > 0){
            throw new LimitExceedException("The Requested amount exceeds the Account balance");
        }
        //Checking to see if the Transaction Pin matches the account to verify Authentication once more
        if(!(passwordEncoder.matches(transferReqDto.transactionPin(), sourceAccount.getTransactionPin()))){
            throw new IncorrectPinException("The Transaction Pin doesn't Match");
        }
        //The specified amount is subtracted from source account balance and added to the Destination account and then saved
        BigDecimal newSourceBalance = sourceAccount.getBalance().subtract(transferReqDto.amount());
        sourceAccount.setBalance(newSourceBalance);
        accountRepository.save(sourceAccount);
        BigDecimal newDestinationBalance = destinationAccount.getBalance().add(transferReqDto.amount());
        destinationAccount.setBalance(newDestinationBalance);
        accountRepository.save(destinationAccount);
        //Creating a Transaction Object recording the Transfer info and Saving it
        Transaction transaction = new Transaction();
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setAmount(transferReqDto.amount());
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setSourceAccountBalance(sourceAccount.getBalance());
        transaction.setDestinationAccountBalance(destinationAccount.getBalance());
        transaction.setCompleted(true);
        transaction=transactionRepository.save(transaction);
        //Returning the Transfer Transaction info
        log.info("Transfer transaction is complete and its info is returned");
        return transactionRepository.getTransferTransactionResp(transaction.getId());
    }

    public TransactionFilterDto dynamicFilterTransaction(Long transactionId, Long sourceAccountId, Long destinationAccountId,
                                                         TransactionType transactionType, BigDecimal minAmount, BigDecimal maxAmount,
                                                         Boolean isCompleted, LocalDate completedFrom, LocalDate completedTill,
                                                         Long sourceCustomerId, Long destinationCustomerId, Integer page,
                                                         Integer size) {
        log.info("Filtering the Transactions");
        //Creating Pageable object for pagination
        Pageable pageable = PageRequest.of(page, size);
        log.info("Returning filtered Transactions");
        Page<TransactionRespDto> pageDto = transactionRepository.dynamicFilterTransaction(transactionId,sourceAccountId,destinationAccountId,transactionType,
                                                              minAmount, maxAmount,isCompleted,
                                                              completedFrom == null ? null: completedFrom.atStartOfDay(ZoneOffset.UTC).toInstant(),
                                                              completedTill == null? null: completedTill.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                                                              sourceCustomerId,destinationCustomerId,pageable);
        return new TransactionFilterDto(pageDto.getContent(),
                                        new PaginationDto(pageDto.getTotalPages(),pageDto.getTotalElements(),pageDto.getNumberOfElements()));
    }

    public TransactionFilterDto dynamicCustomerFilterTransaction(Long transactionId, Long sourceAccountId,
                                                                     Long destinationAccountId, TransactionType transactionType,
                                                                     BigDecimal minAmount, BigDecimal maxAmount, Boolean isCompleted,
                                                                     LocalDate completedFrom, LocalDate completedTill, Integer page,
                                                                     Integer size, String customerUsername) {
        log.info("Filtering the Customer Transactions");
        //Creating Pageable object for pagination
        Pageable pageable = PageRequest.of(page, size);
        //Getting Customer Object to get only that particular Customer's Transaction data
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        Long loggedInId = customer.getId();

        //Getting the raw customer transaction Data and storing it for filtering sensitive data
        Page<TransactionRespDto> securedList = transactionRepository.dynamicFilterTransaction(transactionId,sourceAccountId,
                destinationAccountId,transactionType,minAmount,maxAmount,isCompleted,
                completedFrom == null ? null: completedFrom.atStartOfDay(ZoneOffset.UTC).toInstant(),
                completedTill == null? null: completedTill.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                loggedInId, loggedInId, pageable);

        //Only showing customer their Balance and transaction data
         Page<TransactionRespDto> pageDto=securedList
                .map(dto -> new TransactionRespDto(
                        dto.transactionId(),
                        dto.sourceAccountId(),
                        dto.sourceCustomerId(),
                        dto.sourceCustomerName(),
                        dto.destinationAccountId(),
                        dto.destinationCustomerId(),
                        dto.destinationCustomerName(),
                        dto.amount(),
                        dto.transactionType(),
                        loggedInId.equals(dto.sourceCustomerId())? dto.sourceAccountBalance() : null,
                        loggedInId.equals(dto.destinationCustomerId())? dto.destinationAccountBalance() : null,
                        dto.isCompleted(),
                        dto.completedAt()
                ));
        return new TransactionFilterDto(pageDto.getContent(),
                new PaginationDto(pageDto.getTotalPages(),pageDto.getTotalElements(),pageDto.getNumberOfElements()));
    }

    public StatementDto getStatement(Long accountId, LocalDate fromDate, LocalDate tillDate, String customerUsername) {
        log.info("Generating Account Statement");
        //Getting the logged in Customer
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        //Checking if the given Account ID is valid else throw exception
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account Id is Invalid"));
        //Checking if the given Account ID belongs to Customer else Throw Exception
        if (!(account.getCustomer().getId().equals(customer.getId()))) {
            throw new OwnershipException("The Account does not belong to Customer");
        }

        Instant from = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant periodEnd = tillDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        //Getting all completed transactions of this account up to the end date, oldest first
        List<TransactionRespDto> all = transactionRepository.dynamicFilterTransaction(
                        null, accountId, accountId, null, null, null, true,
                        null, periodEnd, customer.getId(), customer.getId(), Pageable.unpaged())
                .getContent()
                .stream()
                .sorted(Comparator.comparing(TransactionRespDto::completedAt)
                        .thenComparing(TransactionRespDto::transactionId))
                .toList();

        BigDecimal openingBalance = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;
        List<StatementRowDto> rows = new ArrayList<>();

        for (TransactionRespDto t : all) {
            boolean isCredit = accountId.equals(t.destinationAccountId());
            //The balance of this account right after the transaction
            BigDecimal balanceAfter = isCredit ? t.destinationAccountBalance() : t.sourceAccountBalance();

            if (t.completedAt().isBefore(from)) {
                //Before the period, the last one of these is the opening balance
                openingBalance = balanceAfter;
            } else {
                if (isCredit) {
                    totalCredits = totalCredits.add(t.amount());
                } else {
                    totalDebits = totalDebits.add(t.amount());
                }

                String details;
                if (t.transactionType() == TransactionType.DEPOSIT) {
                    details = "Cheque Deposit";
                } else if (t.transactionType() == TransactionType.WITHDRAW) {
                    details = "Cash Withdrawal";
                } else if (isCredit) {
                    details = "Transfer from " + t.sourceAccountId() + " - " + t.sourceCustomerName();
                } else {
                    details = "Transfer to " + t.destinationAccountId() + " - " + t.destinationCustomerName();
                }

                rows.add(new StatementRowDto(
                        t.completedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                        details,
                        isCredit ? null : t.amount(),
                        isCredit ? t.amount() : null,
                        balanceAfter
                ));
            }
        }

        //Ending balance is the balance after the last row, or the opening balance if nothing happened in the period
        BigDecimal endingBalance = rows.isEmpty() ? openingBalance : rows.get(rows.size() - 1).balance();

        log.info("Account Statement is ready and its info is returned");
        return new StatementDto(
                customer.getName(),
                account.getAccountType().toString(),
                account.getId(),
                customer.getAddress(),
                account.getBranch().getBranchName(),
                account.getBranch().getIfscCode(),
                "INR",
                fromDate,
                tillDate,
                openingBalance,
                totalCredits,
                totalDebits,
                endingBalance,
                rows
        );
    }
}
