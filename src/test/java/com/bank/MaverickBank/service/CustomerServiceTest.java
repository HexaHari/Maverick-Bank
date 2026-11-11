package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.CustomerReqDto;
import com.bank.MaverickBank.dto.request.CustomerUpdateDto;
import com.bank.MaverickBank.dto.response.CustomerFilterDto;
import com.bank.MaverickBank.dto.response.CustomerFilterRespDto;
import com.bank.MaverickBank.dto.response.CustomerInfoDto;
import com.bank.MaverickBank.dto.response.CustomerUploadDto;
import com.bank.MaverickBank.dto.response.UploadStatusDto;
import com.bank.MaverickBank.enums.Gender;
import com.bank.MaverickBank.enums.Role;
import com.bank.MaverickBank.exception.ResourceNotFoundException;
import com.bank.MaverickBank.exception.TypeMismatchException;
import com.bank.MaverickBank.model.Customer;
import com.bank.MaverickBank.model.User;
import com.bank.MaverickBank.repository.CustomerRepository;
import com.bank.MaverickBank.utility.JwtUtility;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/*
   Note: addCustomerPicture, addCustomerIdentificationDoc and the successful part of updateProfile save the
   file in to a fixed folder on the disk (uploadDir), so only the cases that stop before saving are tested here.
 */
@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {
    @InjectMocks
    private CustomerService customerService;
    @Mock
    private UserService userService;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private JwtUtility jwtUtility;
    @Mock
    private MultipartFile picture;
    @Mock
    private MultipartFile identificationDoc;

    private Customer createCustomer(Long id,String name){
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setDateOfBirth(LocalDate.of(2004,8,18));
        customer.setGender(Gender.FEMALE);
        customer.setContact("9876543210");
        customer.setAddress("Vellore");
        return customer;
    }

    @Test
    public void addCustomerTest(){
        CustomerReqDto dto = new CustomerReqDto("Monisha",LocalDate.of(2004,8,18),Gender.FEMALE,"9876543210",
                "Vellore","123456789012","ABCDE1234F","monisha","pass123");
        User user = new User("monisha","encodedPass",Role.CUSTOMER);
        user.setId(1L);
        Customer savedCustomer = createCustomer(1L,"Monisha");
        savedCustomer.setUser(user);

        when(userService.getUserObject("monisha","pass123",Role.CUSTOMER)).thenReturn(user);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(jwtUtility.generateToken("monisha")).thenReturn("token123");

        CustomerUploadDto result = customerService.addCustomer(dto);

        assertEquals("token123",result.token());
        assertEquals(Role.CUSTOMER,result.role());

        verify(userService,times(1)).getUserObject("monisha","pass123",Role.CUSTOMER);
        verify(customerRepository,times(1)).save(any(Customer.class));
        verify(jwtUtility,times(1)).generateToken("monisha");

        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(jwtUtility);
    }

    @Test
    public void addCustomerTest_Invalid(){
        CustomerReqDto dto = new CustomerReqDto("Monisha",LocalDate.of(2004,8,18),Gender.FEMALE,"9876543210",
                "Vellore","123456789012",null,"monisha","pass123");

        //for example the username is already taken
        when(userService.getUserObject("monisha","pass123",Role.CUSTOMER)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class,()->customerService.addCustomer(dto));

        //no customer is saved and no token is created
        verify(userService,times(1)).getUserObject("monisha","pass123",Role.CUSTOMER);
        verify(customerRepository,times(0)).save(any(Customer.class));
        verify(jwtUtility,times(0)).generateToken("monisha");

        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(jwtUtility);
    }

    @Test
    public void addCustomerPictureTest_Invalid(){
        Customer customer = createCustomer(1L,"Monisha");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(picture.getOriginalFilename()).thenReturn(null);

        //a file without a name can not be saved
        assertThrows(NullPointerException.class,()->customerService.addCustomerPicture(picture,"monisha"));
        assertNull(customer.getPicture());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(customerRepository,times(0)).save(any(Customer.class));

        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void addCustomerIdentificationDocTest_Invalid(){
        Customer customer = createCustomer(1L,"Monisha");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(identificationDoc.getOriginalFilename()).thenReturn(null);

        assertThrows(NullPointerException.class,()->customerService.addCustomerIdentificationDoc(identificationDoc,"monisha"));
        assertNull(customer.getIdentificationDoc());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(customerRepository,times(0)).save(any(Customer.class));

        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void getUploadStatusTest(){
        Customer customer1 = createCustomer(1L,"Monisha");
        customer1.setPicture("1_pic.jpg");
        customer1.setIdentificationDoc("1_aadhar.pdf");
        Customer customer2 = createCustomer(2L,"Giri");
        customer2.setPicture("2_pic.jpg");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer1);
        when(customerRepository.getCustomerByUsername("giri")).thenReturn(customer2);

        UploadStatusDto result1 = customerService.getUploadStatus("monisha");
        UploadStatusDto result2 = customerService.getUploadStatus("giri");

        assertTrue(result1.pictureUploaded());
        assertTrue(result1.documentUploaded());
        assertTrue(result2.pictureUploaded());
        assertFalse(result2.documentUploaded());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(customerRepository,times(1)).getCustomerByUsername("giri");

        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void getUploadStatusTest_NothingUploaded(){
        Customer customer = createCustomer(1L,"Monisha");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);

        UploadStatusDto result = customerService.getUploadStatus("monisha");

        assertFalse(result.pictureUploaded());
        assertFalse(result.documentUploaded());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");

        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void getCustomerInfoTest(){
        Customer customer = createCustomer(1L,"Monisha");
        customer.setPicture("1_pic.jpg");
        customer.setIdentificationDoc("1_aadhar.pdf");

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);

        CustomerInfoDto result = customerService.getCustomerInfo("monisha");

        assertEquals(1L,result.id());
        assertEquals("Monisha",result.name());
        assertEquals(LocalDate.of(2004,8,18),result.dateOfBirth());
        assertEquals(Gender.FEMALE,result.gender());
        assertEquals("9876543210",result.contact());
        assertEquals("Vellore",result.address());
        assertEquals("1_pic.jpg",result.picture());
        assertEquals("1_aadhar.pdf",result.identificationDoc());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");

        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void updateProfileTest_Invalid_NoDocument(){
        Customer customer = createCustomer(1L,"Monisha");
        CustomerUpdateDto dto = new CustomerUpdateDto("New Name","9111111111","Chennai",null,null);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                ()->customerService.updateProfile("monisha",dto));
        assertEquals("Identification document is required to update the profile",exception.getMessage());
        assertEquals("Monisha",customer.getName());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(customerRepository,times(0)).save(any(Customer.class));

        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void updateProfileTest_Invalid_EmptyDocument(){
        Customer customer = createCustomer(1L,"Monisha");
        CustomerUpdateDto dto = new CustomerUpdateDto("New Name","9111111111","Chennai",null,identificationDoc);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(identificationDoc.isEmpty()).thenReturn(true);

        assertThrows(ResourceNotFoundException.class,()->customerService.updateProfile("monisha",dto));

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(customerRepository,times(0)).save(any(Customer.class));

        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void updateProfileTest_Invalid_DocumentNotPdf(){
        Customer customer = createCustomer(1L,"Monisha");
        CustomerUpdateDto dto = new CustomerUpdateDto("New Name","9111111111","Chennai",null,identificationDoc);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(identificationDoc.isEmpty()).thenReturn(false);
        when(identificationDoc.getContentType()).thenReturn("image/png");

        TypeMismatchException exception = assertThrows(TypeMismatchException.class,
                ()->customerService.updateProfile("monisha",dto));
        assertEquals("Identification document must be a PDF",exception.getMessage());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(customerRepository,times(0)).save(any(Customer.class));

        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void updateProfileTest_Invalid_PictureNotImage(){
        Customer customer = createCustomer(1L,"Monisha");
        CustomerUpdateDto dto = new CustomerUpdateDto("New Name","9111111111","Chennai",picture,identificationDoc);

        when(customerRepository.getCustomerByUsername("monisha")).thenReturn(customer);
        when(identificationDoc.isEmpty()).thenReturn(false);
        when(identificationDoc.getContentType()).thenReturn("application/pdf");
        when(picture.isEmpty()).thenReturn(false);
        when(picture.getContentType()).thenReturn("text/plain");

        TypeMismatchException exception = assertThrows(TypeMismatchException.class,
                ()->customerService.updateProfile("monisha",dto));
        assertEquals("Picture must be an image",exception.getMessage());
        assertEquals("Monisha",customer.getName());

        verify(customerRepository,times(1)).getCustomerByUsername("monisha");
        verify(customerRepository,times(0)).save(any(Customer.class));

        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void dynamicFilterCustomerTest(){
        CustomerFilterRespDto dto1 = new CustomerFilterRespDto(1L,"Monisha",LocalDate.of(2004,8,18),Gender.FEMALE,
                "9876543210","Vellore","123456789012","ABCDE1234F","1_pic.jpg","1_aadhar.pdf");
        CustomerFilterRespDto dto2 = new CustomerFilterRespDto(2L,"Monica",LocalDate.of(2003,5,2),Gender.FEMALE,
                "9876543211","Chennai","123456789013",null,null,null);
        LocalDate dobFrom = LocalDate.of(2000,1,1);
        LocalDate dobTill = LocalDate.of(2005,12,31);

        when(customerRepository.dynamicFilterCustomer(null,"Mon",Gender.FEMALE,null,null,null,null,dobFrom,dobTill,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(dto1,dto2),PageRequest.of(0,5),2));

        CustomerFilterDto result = customerService.dynamicFilterCustomer(null,"Mon",Gender.FEMALE,null,null,null,null,dobFrom,dobTill,0,5);

        assertEquals(2,result.customerList().size());
        assertEquals("Monisha",result.customerList().get(0).name());
        assertEquals(1,result.paginationInfo().totalPages());
        assertEquals(2L,result.paginationInfo().totalRecords());
        assertEquals(2,result.paginationInfo().numRecordsInPage());

        verify(customerRepository,times(1))
                .dynamicFilterCustomer(null,"Mon",Gender.FEMALE,null,null,null,null,dobFrom,dobTill,PageRequest.of(0,5));

        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void dynamicFilterCustomerTest_Invalid(){
        //page size can not be zero, so the repository is never reached
        assertThrows(IllegalArgumentException.class,
                ()->customerService.dynamicFilterCustomer(null,null,null,null,null,null,null,null,null,0,0));

        verifyNoMoreInteractions(customerRepository);
    }
}
