package com.bank.MaverickBank.contoller;

import com.bank.MaverickBank.dto.request.BranchReqDto;
import com.bank.MaverickBank.dto.response.BranchFilterDto;
import com.bank.MaverickBank.dto.response.BranchRespDto;
import com.bank.MaverickBank.model.Branch;
import com.bank.MaverickBank.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class BranchController {
    private final BranchService branchService;

    //adding a branch
    @PostMapping("/api/branch/add")
    public BranchRespDto addBranch(@Valid@RequestBody BranchReqDto branchReqDto){
        return branchService.addBranch(branchReqDto);
    }

    //adding multiple branches
//    @PostMapping("/api/branch/batch/add")
//    public List<BranchRespDto> addBranchesInBatch(@Valid@RequestBody List<BranchReqDto> branchList){
//       return branchService.addBranchesInBatch(branchList);
//    }

    //dynamic filtering through branches
    // BranchController
    @GetMapping("/api/branch/filter")
    public BranchFilterDto dynamicFilterBranch(@RequestParam(name = "branchId", required = false) Long branchId,
                                               @RequestParam(name = "branchName", required = false) String branchName,
                                               @RequestParam(name = "ifscCode", required = false) String ifscCode,
                                               @RequestParam(name = "addedFrom", required = false) LocalDate addedFrom,
                                               @RequestParam(name = "addedTill", required = false) LocalDate addedTill,
                                               @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                               @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {
        return branchService.dynamicFilterBranch(branchId, branchName, ifscCode, addedFrom, addedTill, page, size);
    }
}
