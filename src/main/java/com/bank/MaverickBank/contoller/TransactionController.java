package com.bank.MaverickBank.contoller;

import com.bank.MaverickBank.dto.request.DepositReqDto;
import com.bank.MaverickBank.dto.request.TransferReqDto;
import com.bank.MaverickBank.dto.request.WithdrawReqDto;
import com.bank.MaverickBank.dto.response.*;
import com.bank.MaverickBank.enums.TransactionType;
import com.bank.MaverickBank.model.Customer;
import com.bank.MaverickBank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TransactionController {
    private final TransactionService transactionService;

    //Customer deposits amount in to their account
//    @PostMapping("/api/transaction/deposit")
//    public DepositRespDto depositAmount(@Valid@RequestBody DepositReqDto depositReqDto, Principal principal){
//        return transactionService.depositAmount(depositReqDto,principal.getName());
//    }
    // Customer deposits amount into their account with a check image
    @PostMapping(value = "/api/transaction/deposit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DepositRespDto depositAmount(
            @Valid @RequestPart("data") DepositReqDto depositReqDto,
            @RequestPart("file") MultipartFile file,
            Principal principal) throws IOException {

        // You will need to update your service method signature to accept the checkImage file
        return transactionService.depositAmount(depositReqDto, file, principal.getName());
    }

    //Customer withdraws amount from their account
    @PostMapping("/api/transaction/withdraw")
    public WithdrawRespDto withdrawAmount(@Valid@RequestBody WithdrawReqDto withdrawReqDto, Principal principal){
        return transactionService.withdrawAmount(withdrawReqDto,principal.getName());
    }


    //Customer can transfer amount from their account
    @PostMapping("/api/transaction/transfer")
    public TransferRespDto transferAmount(@Valid@RequestBody TransferReqDto transferReqDto, Principal principal){
        return transactionService.transferAmount(transferReqDto,principal.getName());
    }

    @GetMapping("/api/transaction/filter")
    public TransactionFilterDto dynamicFilterTransaction(@RequestParam(name = "transactionId",required = false) Long transactionId,
                                                             @RequestParam(name = "sourceAccountId",required = false) Long sourceAccountId,
                                                             @RequestParam(name = "destinationAccountId",required = false) Long destinationAccountId,
                                                             @RequestParam(name = "transactionType",required = false)TransactionType transactionType,
                                                             @RequestParam(name = "minAmount",required = false)BigDecimal minAmount,
                                                             @RequestParam(name = "maxAmount",required = false)BigDecimal maxAmount,
                                                             @RequestParam(name = "isCompleted",required = false) Boolean isCompleted,
                                                             @RequestParam(name = "completedFrom",required = false)LocalDate completedFrom,
                                                             @RequestParam(name = "completedTill",required = false)LocalDate completedTill,
                                                             @RequestParam(name = "sourceCustomerId",required = false) Long sourceCustomerId,
                                                             @RequestParam(name = "destinationCustomerId",required = false) Long destinationCustomerId,
                                                             @RequestParam(name = "page",required = false,defaultValue = "0") Integer page,
                                                             @RequestParam(name = "size",required = false,defaultValue = "10") Integer size){
        return transactionService.dynamicFilterTransaction(transactionId,sourceAccountId,destinationAccountId,transactionType,minAmount,
                                                           maxAmount,isCompleted,completedFrom,completedTill,sourceCustomerId,
                                                           destinationCustomerId,page,size);
    }

    @GetMapping("/api/transaction/customer/filter")
    public TransactionFilterDto dynamicCustomerFilterTransaction(@RequestParam(name = "transactionId",required = false) Long transactionId,
                                                                 @RequestParam(name = "sourceAccountId",required = false) Long sourceAccountId,
                                                                 @RequestParam(name = "destinationAccountId",required = false) Long destinationAccountId,
                                                                 @RequestParam(name = "transactionType",required = false)TransactionType transactionType,
                                                                 @RequestParam(name = "minAmount",required = false)BigDecimal minAmount,
                                                                 @RequestParam(name = "maxAmount",required = false)BigDecimal maxAmount,
                                                                 @RequestParam(name = "isCompleted",required = false) Boolean isCompleted,
                                                                 @RequestParam(name = "completedFrom",required = false)LocalDate completedFrom,
                                                                 @RequestParam(name = "completedTill",required = false)LocalDate completedTill,
                                                                 @RequestParam(name = "page",required = false,defaultValue = "0") Integer page,
                                                                 @RequestParam(name = "size",required = false,defaultValue = "10") Integer size,
                                                                 Principal principal){
        return transactionService.dynamicCustomerFilterTransaction(transactionId,sourceAccountId,destinationAccountId,transactionType,minAmount,
                maxAmount,isCompleted,completedFrom,completedTill,page,size,principal.getName());
    }

    @GetMapping("/api/transaction/customer/statement")
    public StatementDto getStatement(@RequestParam Long accountId,
                                     @RequestParam LocalDate fromDate,
                                     @RequestParam LocalDate tillDate,
                                     Principal principal) {
        return transactionService.getStatement(accountId, fromDate, tillDate, principal.getName());
    }

}
