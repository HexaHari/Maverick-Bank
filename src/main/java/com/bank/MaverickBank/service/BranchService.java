package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.BranchReqDto;
import com.bank.MaverickBank.dto.response.BranchFilterDto;
import com.bank.MaverickBank.dto.response.BranchRespDto;
import com.bank.MaverickBank.dto.response.PaginationDto;
import com.bank.MaverickBank.mapper.BranchMapper;
import com.bank.MaverickBank.model.Branch;
import com.bank.MaverickBank.repository.BranchRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class BranchService {
    private final BranchRepository branchRepository;

    public BranchRespDto addBranch(@Valid BranchReqDto branchReqDto) {
        log.info("Initializing addition of Branch to The Database");
        //adding the primitive fields from Dto to Branch
        Branch branch= BranchMapper.mapDtoToModel(branchReqDto);
        // branch is now saved
        branch = branchRepository.save(branch);
        log.info("Completed addition of Branch to The Database");
        // returning branch values for confirmation
        return BranchMapper.mapModelToDto(branch);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<BranchRespDto> addBranchesInBatch(@Valid List<BranchReqDto> list) {
        //adding the primitive fields from Dtos to Branches
        List<Branch> branchList=list.stream().
                map(BranchMapper::mapDtoToModel).
                toList();
        log.info("Processing multiple branches");
        // branches are now saved
        branchList=branchRepository.saveAll(branchList);
        // returning branches values for confirmation
        return branchList.stream().map(BranchMapper::mapModelToDto).toList();
    }

    public Branch getBranchByIfscCode(String ifscCode) {
        log.info("Getting branch by IFSC Code");
        return branchRepository.findBranchByIfscCode(ifscCode);
    }

    // BranchService
    public BranchFilterDto dynamicFilterBranch(Long branchId, String branchName, String ifscCode,
                                               LocalDate addedFrom, LocalDate addedTill,
                                               Integer page, Integer size) {
        log.info("Searching Branches through Dynamic Filter");
        Pageable pageable = PageRequest.of(page, size);
        Page<BranchRespDto> pageDto = branchRepository.dynamicFilterBranch(branchId, branchName, ifscCode,
                addedFrom, addedTill, pageable);
        return new BranchFilterDto(pageDto.getContent(),
                new PaginationDto(pageDto.getTotalPages(), pageDto.getTotalElements(), pageDto.getNumberOfElements()));
    }
}
