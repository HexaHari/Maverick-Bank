package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.response.DetailsDto;
import com.bank.MaverickBank.enums.Role;
import com.bank.MaverickBank.exception.InactiveUserException;
import com.bank.MaverickBank.model.User;
import com.bank.MaverickBank.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void getUserObjectTest(){
        User savedUser = new User("monisha","encodedPass123",Role.CUSTOMER);
        savedUser.setId(1L);

        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.getUserObject("monisha","pass123",Role.CUSTOMER);

        assertEquals(1L,result.getId());
        assertEquals("monisha",result.getUsername());
        assertEquals(Role.CUSTOMER,result.getRole());

        //the user that went to the database must have the encoded password, not the plain one
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(passwordEncoder,times(1)).encode("pass123");
        verify(userRepository,times(1)).save(captor.capture());
        assertEquals("monisha",captor.getValue().getUsername());
        assertEquals("encodedPass123",captor.getValue().getPassword());
        assertEquals(Role.CUSTOMER,captor.getValue().getRole());

        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void getUserDetailsTest(){
        DetailsDto detailsDto = new DetailsDto("monisha",true,Role.CUSTOMER);

        when(userRepository.getUserDetails("monisha")).thenReturn(detailsDto);

        DetailsDto result = userService.getUserDetails("monisha");

        assertEquals("monisha",result.username());
        assertEquals(Role.CUSTOMER,result.role());
        assertTrue(result.isActive());

        verify(userRepository,times(1)).getUserDetails("monisha");

        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void getUserDetailsTest_Invalid(){
        //a deactivated user must not be able to continue
        DetailsDto detailsDto = new DetailsDto("giri",false,Role.CUSTOMER);

        when(userRepository.getUserDetails("giri")).thenReturn(detailsDto);

        InactiveUserException exception = assertThrows(InactiveUserException.class,()->userService.getUserDetails("giri"));
        assertEquals("User is disabled",exception.getMessage());

        verify(userRepository,times(1)).getUserDetails("giri");

        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }
}
