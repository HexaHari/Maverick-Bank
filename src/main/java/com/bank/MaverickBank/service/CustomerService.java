package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.CustomerReqDto;
import com.bank.MaverickBank.dto.request.CustomerUpdateDto;
import com.bank.MaverickBank.dto.response.*;
import com.bank.MaverickBank.enums.Gender;
import com.bank.MaverickBank.enums.Role;
import com.bank.MaverickBank.exception.ResourceNotFoundException;
import com.bank.MaverickBank.exception.TypeMismatchException;
import com.bank.MaverickBank.mapper.CustomerMapper;
import com.bank.MaverickBank.model.Customer;
import com.bank.MaverickBank.model.User;
import com.bank.MaverickBank.repository.CustomerRepository;
import com.bank.MaverickBank.utility.JwtUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomerService {
    private final UserService userService;
    private final CustomerRepository customerRepository;
    private final JwtUtility jwtUtility;
    private final static String uploadDir ="C:/Users/srih8/NodeJavaScriptFsd/MavericBank-ui/public/uploads/";

    public CustomerUploadDto addCustomer(@Valid CustomerReqDto customerReqDto) {
        log.info("Initializing addition of Customer to The Database");
        User user =userService.getUserObject(customerReqDto.username(),
                                             customerReqDto.password(),
                                             Role.CUSTOMER);
        Customer customer= CustomerMapper.mapDtoToModel(customerReqDto);
        customer.setUser(user);
        customer=customerRepository.save(customer);
        String token = jwtUtility.generateToken(customer.getUser().getUsername());
        log.info("Completed addition of Customer to The Database");
        return new CustomerUploadDto(token,
                                      Role.CUSTOMER);
    }

    public void addCustomerPicture(MultipartFile file, String customerUsername) throws IOException {
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        log.info("Uploaded File Name: {}", file.getOriginalFilename());
        log.info("File Size: {}", file.getSize());
        String ext = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];
        log.info("File Extension: {}", ext);
        String originalFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), Paths.get(uploadDir+originalFileName), StandardCopyOption.REPLACE_EXISTING);
        customer.setPicture(originalFileName);
        customerRepository.save(customer);
    }

    public void addCustomerIdentificationDoc(MultipartFile file, String customerUsername) throws IOException {
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        log.info("Uploaded File Name: {}", file.getOriginalFilename());
        log.info("File Size: {}", file.getSize());
        String ext = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];
        log.info("File Extension: {}", ext);
        String originalFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), Paths.get(uploadDir+originalFileName), StandardCopyOption.REPLACE_EXISTING);
        customer.setIdentificationDoc(originalFileName);
        customerRepository.save(customer);
    }

    public UploadStatusDto getUploadStatus(String customerUsername) {
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        return new UploadStatusDto(customer.getPicture() != null,
                customer.getIdentificationDoc() != null);
    }

    public CustomerInfoDto getCustomerInfo(String customerUsername) {
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);
        return CustomerMapper.mapModelToDto(customer);
    }

    @Transactional
    public CustomerInfoDto updateProfile(String customerUsername, CustomerUpdateDto dto) throws IOException{
        Customer customer = customerRepository.getCustomerByUsername(customerUsername);

        if (dto.identificationDoc() == null || dto.identificationDoc().isEmpty()) {
            throw new ResourceNotFoundException("Identification document is required to update the profile");
        }
        if (!"application/pdf".equals(dto.identificationDoc().getContentType())) {
            throw new TypeMismatchException("Identification document must be a PDF");
        }
        if (dto.picture() != null && !dto.picture().isEmpty()) {
            String type = dto.picture().getContentType();
            if (type == null || !type.startsWith("image/")) {
                throw new TypeMismatchException("Picture must be an image");
            }
            customer.setPicture(saveFile(dto.picture()));
        }

        if (dto.name() != null) {
            customer.setName(dto.name());
        }
        if (dto.contact() != null) {
            customer.setContact(dto.contact());
        }
        if (dto.address() != null) {
            customer.setAddress(dto.address());
        }
        customer.setIdentificationDoc(saveFile(dto.identificationDoc()));

        customerRepository.save(customer);
        return getCustomerInfo(customerUsername);
    }

    private String saveFile(MultipartFile file) throws IOException{
        log.info("Uploaded File Name: {}", file.getOriginalFilename());
        log.info("File Size: {}", file.getSize());
        String ext = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];
        log.info("File Extension: {}", ext);
        String originalFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), Paths.get(uploadDir+originalFileName), StandardCopyOption.REPLACE_EXISTING);
        return originalFileName;
    }

    public CustomerFilterDto dynamicFilterCustomer(Long customerId, String name, Gender gender, String contact,
                                                   String aadharNumber, String panNumber, String address,
                                                   LocalDate dobFrom, LocalDate dobTill, Integer page, Integer size) {
        log.info("Searching Customers through Dynamic Filter");
        //Creating Pageable object for Pagination
        Pageable pageable = PageRequest.of(page, size);
        log.info("Returning Filtered Customers");
        //filtering the customers in the repository and returning them
        Page<CustomerFilterRespDto> pageDto = customerRepository.dynamicFilterCustomer(customerId, name, gender,
                contact, aadharNumber, panNumber, address, dobFrom, dobTill, pageable);
        return new CustomerFilterDto(pageDto.getContent(),
                new PaginationDto(pageDto.getTotalPages(), pageDto.getTotalElements(), pageDto.getNumberOfElements()));
    }
}
