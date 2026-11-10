package com.bank.MaverickBank.contoller;

import com.bank.MaverickBank.dto.request.BeneficiaryReqDto;
import com.bank.MaverickBank.dto.response.BeneficiaryFilterDto;
import com.bank.MaverickBank.dto.response.BeneficiaryFilterRespDto;
import com.bank.MaverickBank.dto.response.BeneficiaryRespDto;
import com.bank.MaverickBank.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class BeneficiaryController {
   private final BeneficiaryService beneficiaryService;

    @PostMapping("/api/beneficiary/add")
    public BeneficiaryRespDto addBeneficiary(@Valid@RequestBody BeneficiaryReqDto beneficiaryReqDto, Principal principal){
        return beneficiaryService.addBeneficiary(beneficiaryReqDto,principal.getName());
    }

    @PostMapping("/api/beneficiary/batch/add")
    public List<BeneficiaryRespDto> addBeneficiariesInBatch(@Valid@RequestBody List<BeneficiaryReqDto> beneficiaryReqList, Principal principal){
        return beneficiaryService.addBeneficiariesInBatch(beneficiaryReqList,principal.getName());
    }

    @GetMapping("/api/beneficiary/filter")
    public BeneficiaryFilterDto dynamicFilterBeneficiary(@RequestParam(name = "beneficiaryId",required = false) Long beneficiaryId,
                                                         @RequestParam(name = "beneficiaryAccountId",required = false) Long beneficiaryAccountId,
                                                         @RequestParam(name = "customerId",required = false) Long customerId,
                                                         @RequestParam(name = "beneficiaryName",required = false) String beneficiaryName,
                                                         @RequestParam(name = "beneficiaryIfscCode",required = false) String beneficiaryIfscCode,
                                                         @RequestParam(name = "addedFrom",required = false) LocalDate addedFrom,
                                                         @RequestParam(name = "addedTill",required = false) LocalDate addedTill,
                                                         @RequestParam(name = "page",required = false,defaultValue = "0") Integer page,
                                                         @RequestParam(name = "size",required = false,defaultValue = "10") Integer size){
        return beneficiaryService.dynamicFilterBeneficiary(beneficiaryId,beneficiaryAccountId,customerId,beneficiaryName,beneficiaryIfscCode,
                                                           addedFrom,addedTill,page,size);
    }

    @GetMapping("/api/beneficiary/customer/filter")
    public BeneficiaryFilterDto dynamicCustomerFilterBeneficiary(@RequestParam(name = "beneficiaryId",required = false) Long beneficiaryId,
                                                                 @RequestParam(name = "beneficiaryAccountId",required = false) Long beneficiaryAccountId,
                                                                 @RequestParam(name = "beneficiaryName",required = false) String beneficiaryName,
                                                                 @RequestParam(name = "beneficiaryIfscCode",required = false) String beneficiaryIfscCode,
                                                                 @RequestParam(name = "addedFrom",required = false) LocalDate addedFrom,
                                                                 @RequestParam(name = "addedTill",required = false) LocalDate addedTill,
                                                                 @RequestParam(name = "page",required = false,defaultValue = "0") Integer page,
                                                                 @RequestParam(name = "size",required = false,defaultValue = "10") Integer size,
                                                                 Principal principal){
        return beneficiaryService.dynamicCustomerFilterBeneficiary(beneficiaryId,beneficiaryAccountId,beneficiaryName,beneficiaryIfscCode,
                addedFrom,addedTill,page,size,principal.getName());
    }

    @DeleteMapping("/api/beneficiary/delete/{id}")
    public void deleteBeneficiary(@PathVariable("id") Long beneficiaryId, Principal principal){
        beneficiaryService.deleteBeneficiary(beneficiaryId,principal.getName());
    }
}
