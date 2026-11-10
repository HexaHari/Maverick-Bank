package com.bank.MaverickBank.contoller;

import com.bank.MaverickBank.dto.request.LoanApproveReqDto;
import com.bank.MaverickBank.dto.request.LoanAssignReqDto;
import com.bank.MaverickBank.dto.request.LoanOpenReqDto;
import com.bank.MaverickBank.dto.response.ChartDto;
import com.bank.MaverickBank.dto.response.LoanFilterDto;
import com.bank.MaverickBank.dto.response.LoanRespDto;
import com.bank.MaverickBank.enums.LoanStatus;
import com.bank.MaverickBank.enums.LoanType;
import com.bank.MaverickBank.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class LoanController {
    private final LoanService loanService;

    @PostMapping(value = "/api/loan/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LoanRespDto addLoan(@Valid@ModelAttribute LoanOpenReqDto loanOpenReqDto, Principal principal) throws IOException {
        return loanService.addLoan(loanOpenReqDto, principal.getName());
    }

    @GetMapping("/api/loan/filter")
    public LoanFilterDto dynamicFilterLoan(@RequestParam(name = "loanId", required = false) Long loanId,
                                           @RequestParam(name = "targetAccountId", required = false) Long targetAccountId,
                                           @RequestParam(name = "customerId", required = false) Long customerId,
                                           @RequestParam(name = "loanType", required = false) LoanType loanType,
                                           @RequestParam(name = "loanStatus", required = false) LoanStatus loanStatus,
                                           @RequestParam(name = "minRequestedAmount", required = false) BigDecimal minRequestedAmount,
                                           @RequestParam(name = "maxRequestedAmount", required = false) BigDecimal maxRequestedAmount,
                                           @RequestParam(name = "minApprovedAmount", required = false) BigDecimal minApprovedAmount,
                                           @RequestParam(name = "maxApprovedAmount", required = false) BigDecimal maxApprovedAmount,
                                           @RequestParam(name = "ifscCode", required = false) String ifscCode,
                                           @RequestParam(name = "managingEmployeeId", required = false) Long managingEmployeeId,
                                           @RequestParam(name = "financialVerification", required = false) Boolean financialVerification,
                                           @RequestParam(name = "createdFrom", required = false) LocalDate createdFrom,
                                           @RequestParam(name = "createdTill", required = false) LocalDate createdTill,
                                           @RequestParam(name = "disbursedFrom", required = false) LocalDate disbursedFrom,
                                           @RequestParam(name = "disbursedTill", required = false) LocalDate disbursedTill,
                                           @RequestParam(name = "closedFrom", required = false) LocalDate closedFrom,
                                           @RequestParam(name = "closedTill", required = false) LocalDate closedTill,
                                           @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                           @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {

        return loanService.dynamicFilterLoan(loanId, targetAccountId, customerId, loanType, loanStatus,
                 minRequestedAmount, maxRequestedAmount, minApprovedAmount,
                maxApprovedAmount, ifscCode, managingEmployeeId, financialVerification, createdFrom,
                createdTill, disbursedFrom, disbursedTill, closedFrom, closedTill, page, size);
    }

    @GetMapping("/api/loan/customer/filter")
    public LoanFilterDto dynamicCustomerFilterLoan(@RequestParam(name = "loanId", required = false) Long loanId,
                                           @RequestParam(name = "targetAccountId", required = false) Long targetAccountId,
                                           @RequestParam(name = "loanType", required = false) LoanType loanType,
                                           @RequestParam(name = "loanStatus", required = false) LoanStatus loanStatus,
                                           @RequestParam(name = "minRequestedAmount", required = false) BigDecimal minRequestedAmount,
                                           @RequestParam(name = "maxRequestedAmount", required = false) BigDecimal maxRequestedAmount,
                                           @RequestParam(name = "minApprovedAmount", required = false) BigDecimal minApprovedAmount,
                                           @RequestParam(name = "maxApprovedAmount", required = false) BigDecimal maxApprovedAmount,
                                           @RequestParam(name = "ifscCode", required = false) String ifscCode,
                                           @RequestParam(name = "managingEmployeeId", required = false) Long managingEmployeeId,
                                           @RequestParam(name = "financialVerification", required = false) Boolean financialVerification,
                                           @RequestParam(name = "createdFrom", required = false) LocalDate createdFrom,
                                           @RequestParam(name = "createdTill", required = false) LocalDate createdTill,
                                           @RequestParam(name = "disbursedFrom", required = false) LocalDate disbursedFrom,
                                           @RequestParam(name = "disbursedTill", required = false) LocalDate disbursedTill,
                                           @RequestParam(name = "closedFrom", required = false) LocalDate closedFrom,
                                           @RequestParam(name = "closedTill", required = false) LocalDate closedTill,
                                           @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                           @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
                                           Principal principal) {

        return loanService.dynamicCustomerFilterLoan(loanId, targetAccountId, loanType, loanStatus,
                 minRequestedAmount, maxRequestedAmount, minApprovedAmount,
                maxApprovedAmount, ifscCode, managingEmployeeId, financialVerification, createdFrom,
                createdTill, disbursedFrom, disbursedTill, closedFrom, closedTill, page, size, principal.getName());
    }

    @PutMapping("/api/loan/assign")
    public LoanRespDto assignLoan(@Valid @RequestBody LoanAssignReqDto loanAssignReqDto, Principal principal) {
        return loanService.assignLoan(loanAssignReqDto, principal.getName());
    }

    @PutMapping("/api/loan/approve")
    public LoanRespDto approveLoan(@Valid @RequestBody LoanApproveReqDto loanApproveReqDto, Principal principal) {
        return loanService.approveLoan(loanApproveReqDto, principal.getName());
    }

    @PutMapping("/api/loan/reject/{loanId}")
    public LoanRespDto rejectLoan(@PathVariable Long loanId, Principal principal) {
        return loanService.rejectLoan(loanId, principal.getName());
    }

    @PutMapping("/api/loan/customer/accept/{loanId}")
    public LoanRespDto acceptLoan(@PathVariable Long loanId, Principal principal) {
        return loanService.acceptLoan(loanId, principal.getName());
    }

    @PutMapping("/api/loan/customer/reject/{loanId}")
    public LoanRespDto customerRejectLoan(@PathVariable Long loanId, Principal principal) {
        return loanService.customerRejectLoan(loanId, principal.getName());
    }

    @PutMapping("/api/loan/disburse/{loanId}")
    public LoanRespDto disburseLoan(@PathVariable Long loanId, Principal principal) {
        return loanService.disburseLoan(loanId, principal.getName());
    }

    @PutMapping("/api/loan/manager/reject/{loanId}")
    public LoanRespDto managerRejectLoan(@PathVariable Long loanId, Principal principal) {
        return loanService.managerRejectLoan(loanId, principal.getName());
    }

    @PutMapping("/api/loan/complete/{loanId}")
    public LoanRespDto completeLoan(@PathVariable Long loanId, Principal principal) {
        return loanService.completeLoan(loanId, principal.getName());
    }

    @GetMapping("/api/loan/status/stat")
    public ChartDto getLoanStatusStats(Principal principal) {
        return loanService.getLoanStatusStats(principal);
    }

    @GetMapping("/api/loan/type/stat")
    public ChartDto getLoanTypeStats(Principal principal) {
        return loanService.getLoanTypeStats(principal);
    }
}
