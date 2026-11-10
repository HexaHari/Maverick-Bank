package com.bank.MaverickBank.mapper;

import com.bank.MaverickBank.dto.request.CustomerReqDto;
import com.bank.MaverickBank.dto.response.CustomerInfoDto;
import com.bank.MaverickBank.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public static Customer mapDtoToModel(CustomerReqDto customerReqDto){
        Customer customer=new Customer();
        customer.setName(customerReqDto.name());
        customer.setDateOfBirth(customerReqDto.dateOfBirth());
        customer.setGender(customerReqDto.gender());
        customer.setContact(customerReqDto.contact());
        customer.setAddress(customerReqDto.address());
        customer.setAadharNumber(customerReqDto.aadharNumber());
        customer.setPanNumber(customerReqDto.panNumber());
        return customer;
    }
      public static CustomerInfoDto mapModelToDto(Customer customer){
        return new CustomerInfoDto(
                customer.getId(),
                customer.getName(),
                customer.getDateOfBirth(),
                customer.getGender(),
                customer.getContact(),
                customer.getAddress(),
                customer.getPicture(),
                customer.getIdentificationDoc()
        );
      }
}
