package com.bank.MaverickBank.mapper;

import com.bank.MaverickBank.dto.request.BranchReqDto;
import com.bank.MaverickBank.dto.response.BranchRespDto;
import com.bank.MaverickBank.model.Branch;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {
    public static Branch mapDtoToModel(BranchReqDto branchReqDto){
        Branch branch =new Branch();
        branch.setBranchName(branchReqDto.branchName());
        branch.setIfscCode(branchReqDto.ifscCode());
        return branch;
    }

    public static BranchRespDto mapModelToDto(Branch branch){
        return new BranchRespDto(
                branch.getId(),
                branch.getBranchName(),
                branch.getIfscCode(),
                branch.getAddedAt()
        );
    }
}
