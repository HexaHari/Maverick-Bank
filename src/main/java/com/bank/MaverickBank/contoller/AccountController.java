package com.bank.MaverickBank.contoller;

import com.bank.MaverickBank.dto.request.AccountCloseReqDto;
import com.bank.MaverickBank.dto.request.AccountOpenReqDto;
import com.bank.MaverickBank.dto.response.AccountFilterDto;
import com.bank.MaverickBank.dto.response.AccountRespDto;
import com.bank.MaverickBank.dto.response.AccountStatusRespDto;
import com.bank.MaverickBank.dto.response.ChartDto;
import com.bank.MaverickBank.enums.AccountType;
import com.bank.MaverickBank.enums.RequestStatus;
import com.bank.MaverickBank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class AccountController {

    private final AccountService accountService;

    //to request the activation of an account by customer
    @PostMapping("/api/account/add")
    public void addAccount(@Valid@RequestBody AccountOpenReqDto accountOpenReqDto, Principal principal){
        accountService.addAccount(accountOpenReqDto,principal.getName());
    }

    //to request the closure of an active account
    @PutMapping("/api/account/close")
    public AccountRespDto closeAccount(@Valid@RequestBody AccountCloseReqDto accountCloseReqDto, Principal principal){
        return accountService.closeAccount(accountCloseReqDto,principal.getName());
    }

    //dynamic filter for fetching accounts, commonly done by Employees
    @GetMapping("/api/account/filter")
    public AccountFilterDto dynamicFilterAccount(@RequestParam(name = "accountType", required = false)AccountType accountType,
                                                     @RequestParam(name = "minBalance", required = false)BigDecimal minBalance,
                                                     @RequestParam(name = "maxBalance", required = false)BigDecimal maxBalance,
                                                     @RequestParam(name = "isActive", required = false)Boolean isActive,
                                                     @RequestParam(name = "requestStatus", required = false) RequestStatus requestStatus,
                                                     @RequestParam(name = "createdFrom", required = false) LocalDate createdFrom,
                                                     @RequestParam(name = "createdTill", required = false) LocalDate createdTill,
                                                     @RequestParam(name = "activatedFrom", required = false) LocalDate activatedFrom,
                                                     @RequestParam(name = "activatedTill", required = false) LocalDate activatedTill,
                                                     @RequestParam(name = "ifscCode", required = false) String ifscCode,
                                                     @RequestParam(name = "customerId",required = false) Long customerId,
                                                     @RequestParam(name = "accountId",required = false) Long accountId,
                                                     @RequestParam(name = "requestedFrom", required = false) LocalDate requestedFrom,
                                                     @RequestParam(name = "requestedTill", required = false) LocalDate requestedTill,
                                                     @RequestParam(name = "page",required = false,defaultValue = "0")Integer page,
                                                     @RequestParam(name = "size",required = false,defaultValue = "10")Integer size){

        return accountService.dynamicFilterAccount(accountType,minBalance,maxBalance,isActive,
                requestStatus,createdFrom,createdTill,activatedFrom,activatedTill,ifscCode,customerId,
                accountId,requestedFrom,requestedTill,page,size);
    }

    //Activates or Deactivates accounts by ID
    @PutMapping("/api/account/request")
    public AccountStatusRespDto handleAccount(@RequestParam(name = "accountId") Long accountId,
                                              @RequestParam(name = "activate") Boolean activate,
                                              Principal principal ){
        return accountService.handleAccount(accountId,activate,principal.getName());
    }

    // customer can only filter and see their accounts
    @GetMapping("/api/account/customer/filter")
    public AccountFilterDto dynamicFilterCustomerAccount(@RequestParam(name = "accountType", required = false)AccountType accountType,
                                                         @RequestParam(name = "minBalance", required = false)BigDecimal minBalance,
                                                         @RequestParam(name = "maxBalance", required = false)BigDecimal maxBalance,
                                                         @RequestParam(name = "isActive", required = false)Boolean isActive,
                                                         @RequestParam(name = "requestStatus", required = false) RequestStatus requestStatus,
                                                         @RequestParam(name = "createdFrom", required = false) LocalDate createdFrom,
                                                         @RequestParam(name = "createdTill", required = false) LocalDate createdTill,
                                                         @RequestParam(name = "activatedFrom", required = false) LocalDate activatedFrom,
                                                         @RequestParam(name = "activatedTill", required = false) LocalDate activatedTill,
                                                         @RequestParam(name = "ifscCode", required = false) String ifscCode,
                                                         @RequestParam(name = "accountId",required = false) Long accountId,
                                                         @RequestParam(name = "requestedFrom", required = false) LocalDate requestedFrom,
                                                         @RequestParam(name = "requestedTill", required = false) LocalDate requestedTill,
                                                         @RequestParam(name = "page",required = false,defaultValue = "0")Integer page,
                                                         @RequestParam(name = "size",required = false,defaultValue = "10")Integer size,
                                                         Principal principal){

        return accountService.dynamicFilterCustomerAccount(accountType,minBalance,maxBalance,isActive,
                requestStatus,createdFrom,createdTill,activatedFrom,activatedTill,ifscCode,accountId,requestedFrom,requestedTill,page,size,principal.getName());
    }

    @GetMapping("/api/account/request-status/stat")
    public ChartDto getAccountRequestStatusStats(Principal principal) {
        return accountService.getAccountRequestStatusStats(principal);
    }

}
