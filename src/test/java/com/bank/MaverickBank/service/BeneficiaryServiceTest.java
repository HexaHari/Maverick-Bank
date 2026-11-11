package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.BeneficiaryReqDto;
import com.bank.MaverickBank.dto.response.BeneficiaryFilterDto;
import com.bank.MaverickBank.dto.response.BeneficiaryFilterRespDto;
import com.bank.MaverickBank.dto.response.BeneficiaryRespDto;
import com.bank.MaverickBank.exception.OwnershipException;
import com.bank.MaverickBank.exception.ResourceNotFoundException;
import com.bank.MaverickBank.exception.UnchangedStateException;
import com.bank.MaverickBank.model.Account;
import com.bank.MaverickBank.model.Beneficiary;
import com.bank.MaverickBank.model.Branch;
import com.bank.MaverickBank.model.Customer;
import com.bank.MaverickBank.repository.AccountRepository;
import com.bank.MaverickBank.repository.BeneficiaryRepository;
import com.bank.MaverickBank.repository.CustomerRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class BeneficiaryServiceTest {
    @InjectMocks
    private BeneficiaryService beneficiaryService;
    @Mock
    private BeneficiaryRepository beneficiaryRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AccountRepository accountRepository;

    private Customer createCustomer(Long id,String name){
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        return customer;
    }

    private Account createAccount(Long id,Customer owner){
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setBranchName("Chennai Main");
        Account account = new Account();
        account.setId(id);
        account.setCustomer(owner);
        account.setBranch(branch);
        return account;
    }

    private Beneficiary createSavedBeneficiary(Long id,String name,Account account,Customer customer){
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(id);
        beneficiary.setBeneficiaryName(name);
        beneficiary.setBeneficiaryAccount(account);
        beneficiary.setCustomer(customer);
        beneficiary.setAddedAt(LocalDate.of(2026,11,10));
        return beneficiary;
    }


    @Test
    public void addBeneficiaryTest(){
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(12L,createCustomer(8L,"Ravi"));
        BeneficiaryReqDto dto = new BeneficiaryReqDto(12L,"My friend Ravi");
        Beneficiary saved = createSavedBeneficiary(1L,"My friend Ravi",account,customer);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(12L)).thenReturn(Optional.of(account));
        when(beneficiaryRepository.getByAccountAndCustomerId(12L,7L)).thenReturn(null);
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenReturn(saved);

        BeneficiaryRespDto result = beneficiaryService.addBeneficiary(dto,"monisha");

        assertEquals(1L,result.beneficiaryId());
        assertEquals(12L,result.beneficiaryAccountId());
        assertEquals("My friend Ravi",result.beneficiaryName());
        assertEquals("Chennai Main",result.beneficiaryBranchName());
        assertEquals(7L,result.customerId());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(12L);
        verify(beneficiaryRepository,times(1)).getByAccountAndCustomerId(12L,7L);
        verify(beneficiaryRepository,times(1)).save(any(Beneficiary.class));

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void addBeneficiaryTest_BlankName(){
        //when the customer gives no name, the name of the account holder is used
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(12L,createCustomer(8L,"Ravi"));
        BeneficiaryReqDto dto = new BeneficiaryReqDto(12L,"  ");
        Beneficiary saved = createSavedBeneficiary(1L,"Ravi",account,customer);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(12L)).thenReturn(Optional.of(account));
        when(beneficiaryRepository.getByAccountAndCustomerId(12L,7L)).thenReturn(null);
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenReturn(saved);

        BeneficiaryRespDto result = beneficiaryService.addBeneficiary(dto,"monisha");

        assertEquals("Ravi",result.beneficiaryName());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(12L);
        verify(beneficiaryRepository,times(1)).getByAccountAndCustomerId(12L,7L);
        verify(beneficiaryRepository,times(1)).save(any(Beneficiary.class));

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void addBeneficiaryTest_InvalidAccount(){
        Customer customer = createCustomer(7L,"Monisha");
        BeneficiaryReqDto dto = new BeneficiaryReqDto(99L,"Ravi");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->beneficiaryService.addBeneficiary(dto,"monisha"));
        assertEquals("Beneficiary Account ID is Invalid",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(99L);
        verify(beneficiaryRepository,times(0)).save(any(Beneficiary.class));

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void addBeneficiaryTest_AlreadyExists(){
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(12L,createCustomer(8L,"Ravi"));
        BeneficiaryReqDto dto = new BeneficiaryReqDto(12L,"Ravi");
        Beneficiary existing = createSavedBeneficiary(1L,"Ravi",account,customer);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(12L)).thenReturn(Optional.of(account));
        when(beneficiaryRepository.getByAccountAndCustomerId(12L,7L)).thenReturn(existing);

        UnchangedStateException exception = assertThrows(UnchangedStateException.class,
                ()->beneficiaryService.addBeneficiary(dto,"monisha"));
        assertEquals("Beneficiary Already Exists",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(12L);
        verify(beneficiaryRepository,times(1)).getByAccountAndCustomerId(12L,7L);
        verify(beneficiaryRepository,times(0)).save(any(Beneficiary.class));

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void addBeneficiariesInBatchTest(){
        Customer customer = createCustomer(7L,"Monisha");
        Account account1 = createAccount(12L,createCustomer(8L,"Ravi"));
        Account account2 = createAccount(13L,createCustomer(9L,"Giri"));
        List<BeneficiaryReqDto> list = List.of(new BeneficiaryReqDto(12L,"Friend Ravi"),new BeneficiaryReqDto(13L,null));

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(12L)).thenReturn(Optional.of(account1));
        when(accountRepository.findById(13L)).thenReturn(Optional.of(account2));
        when(beneficiaryRepository.getByAccountAndCustomerId(12L,7L)).thenReturn(null);
        when(beneficiaryRepository.getByAccountAndCustomerId(13L,7L)).thenReturn(null);
        Beneficiary saved1 = createSavedBeneficiary(1L,"Friend Ravi",account1,customer);
        Beneficiary saved2 = createSavedBeneficiary(2L,"Giri",account2,customer);
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenReturn(saved1,saved2);

        List<BeneficiaryRespDto> result = beneficiaryService.addBeneficiariesInBatch(list,"monisha");

        assertEquals(2,result.size());
        assertEquals(12L,result.get(0).beneficiaryAccountId());
        assertEquals("Friend Ravi",result.get(0).beneficiaryName());
        assertEquals(13L,result.get(1).beneficiaryAccountId());
        assertEquals("Giri",result.get(1).beneficiaryName());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(12L);
        verify(accountRepository,times(1)).findById(13L);
        verify(beneficiaryRepository,times(1)).getByAccountAndCustomerId(12L,7L);
        verify(beneficiaryRepository,times(1)).getByAccountAndCustomerId(13L,7L);
        verify(beneficiaryRepository,times(2)).save(any(Beneficiary.class));

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void addBeneficiariesInBatchTest_InvalidAccount(){
        Customer customer = createCustomer(7L,"Monisha");
        List<BeneficiaryReqDto> list = List.of(new BeneficiaryReqDto(99L,"Ravi"));

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,()->beneficiaryService.addBeneficiariesInBatch(list,"monisha"));

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(99L);
        verify(beneficiaryRepository,times(0)).save(any(Beneficiary.class));

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void addBeneficiariesInBatchTest_AlreadyExists(){
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(12L,createCustomer(8L,"Ravi"));
        List<BeneficiaryReqDto> list = List.of(new BeneficiaryReqDto(12L,"Ravi"));
        Beneficiary existing = createSavedBeneficiary(1L,"Ravi",account,customer);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(accountRepository.findById(12L)).thenReturn(Optional.of(account));
        when(beneficiaryRepository.getByAccountAndCustomerId(12L,7L)).thenReturn(existing);

        assertThrows(UnchangedStateException.class,()->beneficiaryService.addBeneficiariesInBatch(list,"monisha"));

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(accountRepository,times(1)).findById(12L);
        verify(beneficiaryRepository,times(1)).getByAccountAndCustomerId(12L,7L);
        verify(beneficiaryRepository,times(0)).save(any(Beneficiary.class));

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void dynamicFilterBeneficiaryTest(){
        BeneficiaryFilterRespDto dto1 = new BeneficiaryFilterRespDto(1L,12L,"Friend Ravi","Ravi","MAVB0000301","Chennai Main",7L,"Monisha",LocalDate.of(2026,11,10));
        BeneficiaryFilterRespDto dto2 = new BeneficiaryFilterRespDto(2L,13L,"Giri","Giri","MAVB0000301","Chennai Main",8L,"Draco",LocalDate.of(2026,11,11));
        LocalDate addedFrom = LocalDate.of(2026,11,1);
        LocalDate addedTill = LocalDate.of(2026,11,30);

        when(beneficiaryRepository.dynamicFilterBeneficiary(null,null,null,"G","MAVB0000301",addedFrom,addedTill,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(dto1,dto2),PageRequest.of(0,5),2));

        BeneficiaryFilterDto result = beneficiaryService.dynamicFilterBeneficiary(null,null,null,"G","MAVB0000301",addedFrom,addedTill,0,5);

        assertEquals(2,result.beneficiaryList().size());
        assertEquals("Friend Ravi",result.beneficiaryList().get(0).beneficiaryName());
        assertEquals(1,result.paginationInfo().totalPages());
        assertEquals(2L,result.paginationInfo().totalRecords());
        assertEquals(2,result.paginationInfo().numRecordsInPage());

        verify(beneficiaryRepository,times(1))
                .dynamicFilterBeneficiary(null,null,null,"G","MAVB0000301",addedFrom,addedTill,PageRequest.of(0,5));

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void dynamicFilterBeneficiaryTest_Invalid(){
        //page size can not be zero, so the repository is never reached
        assertThrows(IllegalArgumentException.class,
                ()->beneficiaryService.dynamicFilterBeneficiary(null,null,null,null,null,null,null,0,0));

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void dynamicCustomerFilterBeneficiaryTest(){
        Customer customer = createCustomer(7L,"Monisha");
        BeneficiaryFilterRespDto dto1 = new BeneficiaryFilterRespDto(1L,12L,"Friend Ravi","Ravi","MAVB0000301","Chennai Main",7L,"Monisha",LocalDate.of(2026,11,10));

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        //the customer id comes from the logged in customer, so a customer can only see own beneficiaries
        when(beneficiaryRepository.dynamicFilterBeneficiary(null,null,7L,null,null,null,null,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(dto1),PageRequest.of(0,5),1));

        BeneficiaryFilterDto result = beneficiaryService.dynamicCustomerFilterBeneficiary(null,null,null,null,null,null,0,5,"monisha");

        assertEquals(1,result.beneficiaryList().size());
        assertEquals(7L,result.beneficiaryList().get(0).customerId());
        assertEquals(1,result.paginationInfo().totalPages());
        assertEquals(1L,result.paginationInfo().totalRecords());
        assertEquals(1,result.paginationInfo().numRecordsInPage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(beneficiaryRepository,times(1))
                .dynamicFilterBeneficiary(null,null,7L,null,null,null,null,PageRequest.of(0,5));

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void deleteBeneficiaryTest(){
        Customer customer = createCustomer(7L,"Monisha");
        Account account = createAccount(12L,createCustomer(8L,"Ravi"));
        Beneficiary beneficiary = createSavedBeneficiary(1L,"Ravi",account,customer);

        when(beneficiaryRepository.findById(1L)).thenReturn(Optional.of(beneficiary));
        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);

        beneficiaryService.deleteBeneficiary(1L,"monisha");

        verify(beneficiaryRepository,times(1)).findById(1L);
        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(beneficiaryRepository,times(1)).delete(beneficiary);

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void deleteBeneficiaryTest_InvalidBeneficiary(){
        when(beneficiaryRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->beneficiaryService.deleteBeneficiary(99L,"monisha"));
        assertEquals("Beneficiary ID is invalid",exception.getMessage());

        verify(beneficiaryRepository,times(1)).findById(99L);
        verify(beneficiaryRepository,times(0)).delete(any(Beneficiary.class));

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void deleteBeneficiaryTest_NotOwner(){
        //monisha can not delete the beneficiary of another customer
        Customer owner = createCustomer(8L,"Ravi");
        Customer loggedInCustomer = createCustomer(7L,"Monisha");
        Account account = createAccount(12L,createCustomer(9L,"Giri"));
        Beneficiary beneficiary = createSavedBeneficiary(1L,"Giri",account,owner);

        when(beneficiaryRepository.findById(1L)).thenReturn(Optional.of(beneficiary));
        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(loggedInCustomer);

        OwnershipException exception = assertThrows(OwnershipException.class,
                ()->beneficiaryService.deleteBeneficiary(1L,"monisha"));
        assertEquals("Beneficiary does not belong to Customer",exception.getMessage());

        verify(beneficiaryRepository,times(1)).findById(1L);
        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(beneficiaryRepository,times(0)).delete(any(Beneficiary.class));

        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }
}
