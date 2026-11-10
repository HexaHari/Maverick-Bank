package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.BeneficiaryReqDto;
import com.bank.MaverickBank.dto.response.BeneficiaryFilterDto;
import com.bank.MaverickBank.dto.response.BeneficiaryFilterRespDto;
import com.bank.MaverickBank.dto.response.BeneficiaryRespDto;
import com.bank.MaverickBank.dto.response.PaginationDto;
import com.bank.MaverickBank.exception.OwnershipException;
import com.bank.MaverickBank.exception.ResourceNotFoundException;
import com.bank.MaverickBank.exception.UnchangedStateException;
import com.bank.MaverickBank.mapper.BeneficiaryMapper;
import com.bank.MaverickBank.model.Account;
import com.bank.MaverickBank.model.Beneficiary;
import com.bank.MaverickBank.model.Customer;
import com.bank.MaverickBank.repository.AccountRepository;
import com.bank.MaverickBank.repository.BeneficiaryRepository;
import com.bank.MaverickBank.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeneficiaryService {
    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public BeneficiaryRespDto addBeneficiary(@Valid BeneficiaryReqDto beneficiaryReqDto, String customerUsername) {
        log.info("Initializing The addition of another Beneficiary");
        //Getting the Customer for setting the customer ID as it is easier to recognize whose Beneficiary it is
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        //Getting the account for which the Beneficiary is made, if its not found throw Exception
        Account account = accountRepository.findById(beneficiaryReqDto.beneficiaryAccountId())
                          .orElseThrow(()-> new ResourceNotFoundException("Beneficiary Account ID is Invalid"));
        //Checking if this particular Beneficiary already exist between the Customer and Beneficiary, if it does throw Exception
        Beneficiary existingBeneficiary=beneficiaryRepository.getByAccountAndCustomerId(account.getId(),customer.getId());
        if(existingBeneficiary != null)
            throw new UnchangedStateException("Beneficiary Already Exists");
        //Creating new Beneficiary Object and adding the required field values in it
        Beneficiary beneficiary =new Beneficiary();
        beneficiary.setCustomer(customer);
        beneficiary.setBeneficiaryAccount(account);
        //Checking if the Customer added the Beneficiary name or auto-update the Beneficiary name with their actual Customer name
        if(beneficiaryReqDto.beneficiaryName() != null && !beneficiaryReqDto.beneficiaryName().isBlank())
            beneficiary.setBeneficiaryName(beneficiaryReqDto.beneficiaryName());
        else
            beneficiary.setBeneficiaryName(account.getCustomer().getName());
        beneficiary = beneficiaryRepository.save(beneficiary);
        log.info("New Beneficiary is added and returns Info");
        return BeneficiaryMapper.mapModelToDto(beneficiary);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<BeneficiaryRespDto> addBeneficiariesInBatch(@Valid List<BeneficiaryReqDto> beneficiaryReqList, String customerUsername) {
        log.info("Initializing The addition of Beneficiary in Batch");
        //Getting the Customer for setting the customer ID as it is easier to recognize whose Beneficiary it is
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        log.info("New Beneficiaries are added in Batch and returns Info");
        //Adding Multiple Beneficiaries at the same time
          return beneficiaryReqList.stream().
                map(beneficiaryReqDto -> {
                    Account account = accountRepository.findById(beneficiaryReqDto.beneficiaryAccountId())
                            .orElseThrow(()-> new ResourceNotFoundException("Beneficiary Account ID is Invalid"));
                    Beneficiary existingBeneficiary=beneficiaryRepository.getByAccountAndCustomerId(account.getId(),customer.getId());
                    if(existingBeneficiary != null)
                        throw new UnchangedStateException("Beneficiary Already Exists");
                    Beneficiary beneficiary =new Beneficiary();
                    beneficiary.setCustomer(customer);
                    beneficiary.setBeneficiaryAccount(account);
                    if(beneficiaryReqDto.beneficiaryName() != null && !beneficiaryReqDto.beneficiaryName().isBlank())
                        beneficiary.setBeneficiaryName(beneficiaryReqDto.beneficiaryName());
                    else
                        beneficiary.setBeneficiaryName(account.getCustomer().getName());
                    beneficiary = beneficiaryRepository.save(beneficiary);
                    return BeneficiaryMapper.mapModelToDto(beneficiary);
                }).toList();
    }

    public BeneficiaryFilterDto dynamicFilterBeneficiary(Long beneficiaryId, Long beneficiaryAccountId, Long customerId,
                                                                   String beneficiaryName, String beneficiaryIfscCode,
                                                                   LocalDate addedFrom, LocalDate addedTill, Integer page,
                                                                   Integer size) {
        log.info("Initializing the Filtering through Beneficiaries");
        //Creating Pageable object for Pagination
        Pageable pageable = PageRequest.of(page,size);
        log.info("Returning the filtered Beneficiaries");
        Page<BeneficiaryFilterRespDto> pageDto = beneficiaryRepository.dynamicFilterBeneficiary(beneficiaryId,beneficiaryAccountId,customerId,beneficiaryName,
                                                              beneficiaryIfscCode,addedFrom,addedTill,pageable);
        return new BeneficiaryFilterDto(pageDto.getContent(),
                                        new PaginationDto(pageDto.getTotalPages(), pageDto.getTotalElements(), pageDto.getNumberOfElements()));
    }

    public BeneficiaryFilterDto dynamicCustomerFilterBeneficiary(Long beneficiaryId, Long beneficiaryAccountId,
                                                                           String beneficiaryName, String beneficiaryIfscCode,
                                                                           LocalDate addedFrom, LocalDate addedTill, Integer page,
                                                                           Integer size, String customerUsername) {
        log.info("Initializing the Filtering through Beneficiaries for Customer");
        //Getting the Customer for setting the customer ID as it so they can only see theirs
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        //Creating Pageable object for Pagination
        Pageable pageable = PageRequest.of(page,size);
        log.info("Returning the filtered Customer Beneficiaries");
        Page<BeneficiaryFilterRespDto> pageDto = beneficiaryRepository.dynamicFilterBeneficiary(beneficiaryId,beneficiaryAccountId,customer.getId(),beneficiaryName,
                beneficiaryIfscCode,addedFrom,addedTill,pageable);
        return new BeneficiaryFilterDto(pageDto.getContent(),
                new PaginationDto(pageDto.getTotalPages(), pageDto.getTotalElements(), pageDto.getNumberOfElements()));
    }

    public void deleteBeneficiary(Long beneficiaryId, String customerUsername) {
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(()-> new ResourceNotFoundException("Beneficiary ID is invalid"));
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        if(!(beneficiary.getCustomer().getId().equals(customer.getId())))
            throw new OwnershipException("Beneficiary does not belong to Customer");
        beneficiaryRepository.delete(beneficiary);
    }
}
