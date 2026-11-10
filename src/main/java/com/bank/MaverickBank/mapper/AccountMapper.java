package com.bank.MaverickBank.mapper;

import com.bank.MaverickBank.dto.request.AccountOpenReqDto;
import com.bank.MaverickBank.model.Account;
import org.springframework.stereotype.Component;



@Component
public class AccountMapper {

    public static Account mapDtoToModel(AccountOpenReqDto accountOpenReqDto){
        Account account=new Account();
        account.setAccountType(accountOpenReqDto.accountType());
        account.setRequestReason(accountOpenReqDto.requestReason());
        return account;
    }
}
