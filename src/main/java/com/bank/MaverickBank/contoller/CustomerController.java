package com.bank.MaverickBank.contoller;

import com.bank.MaverickBank.dto.request.CustomerReqDto;
import com.bank.MaverickBank.dto.request.CustomerUpdateDto;
import com.bank.MaverickBank.dto.response.CustomerFilterDto;
import com.bank.MaverickBank.dto.response.CustomerInfoDto;
import com.bank.MaverickBank.dto.response.CustomerUploadDto;
import com.bank.MaverickBank.dto.response.UploadStatusDto;
import com.bank.MaverickBank.enums.Gender;
import com.bank.MaverickBank.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerController {

    private final CustomerService customerService;
    @PostMapping("/api/customer/add")
    public CustomerUploadDto addCustomer(@Valid@RequestBody CustomerReqDto customerReqDto){
        return customerService.addCustomer(customerReqDto);
    }

    @PostMapping("/api/customer/picture")
    public void addCustomerPicture(@RequestParam("file")MultipartFile file, Principal principal) throws IOException {
        customerService.addCustomerPicture(file,principal.getName());
    }

    @PostMapping("/api/customer/identification-doc")
    public void addCustomerIdentificationDoc(@RequestParam("file")MultipartFile file, Principal principal) throws IOException {
        customerService.addCustomerIdentificationDoc(file,principal.getName());
    }

    @GetMapping("/api/customer/upload-status")
    public UploadStatusDto getUploadStatus(Principal principal) {
        return customerService.getUploadStatus(principal.getName());
    }

    @GetMapping("/api/customer/profile")
    public CustomerInfoDto getCustomerInfo(Principal principal) {
        return customerService.getCustomerInfo(principal.getName());
    }

    @PutMapping(value = "/api/customer/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CustomerInfoDto updateProfile(Principal principal, @Valid @ModelAttribute CustomerUpdateDto dto) throws IOException{
        return customerService.updateProfile(principal.getName(), dto);
    }

    @GetMapping("/api/customer/filter")
    public CustomerFilterDto dynamicFilterCustomer(@RequestParam(name = "customerId", required = false) Long customerId,
                                                   @RequestParam(name = "name", required = false) String name,
                                                   @RequestParam(name = "gender", required = false) Gender gender,
                                                   @RequestParam(name = "contact", required = false) String contact,
                                                   @RequestParam(name = "aadharNumber", required = false) String aadharNumber,
                                                   @RequestParam(name = "panNumber", required = false) String panNumber,
                                                   @RequestParam(name = "address", required = false) String address,
                                                   @RequestParam(name = "dobFrom", required = false) LocalDate dobFrom,
                                                   @RequestParam(name = "dobTill", required = false) LocalDate dobTill,
                                                   @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                                   @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {

        return customerService.dynamicFilterCustomer(customerId, name, gender, contact, aadharNumber,
                panNumber, address, dobFrom, dobTill, page, size);
    }
}
