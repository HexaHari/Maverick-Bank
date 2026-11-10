package com.bank.MaverickBank.mapper;

import com.bank.MaverickBank.dto.response.BeneficiaryRespDto;
import com.bank.MaverickBank.dto.response.BranchRespDto;
import com.bank.MaverickBank.model.Beneficiary;
import org.springframework.stereotype.Component;

@Component
public class BeneficiaryMapper {
    public static BeneficiaryRespDto mapModelToDto(Beneficiary beneficiary){
        return new BeneficiaryRespDto(
                beneficiary.getId(),
                beneficiary.getBeneficiaryAccount().getId(),
                beneficiary.getBeneficiaryName(),
                beneficiary.getBeneficiaryAccount().getBranch().getBranchName(),
                beneficiary.getAddedAt(),
                beneficiary.getCustomer().getId()
        );
    }
}
