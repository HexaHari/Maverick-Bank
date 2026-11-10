package com.bank.MaverickBank.service;

import com.bank.MaverickBank.enums.Role;
import com.bank.MaverickBank.model.User;
import com.bank.MaverickBank.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
public class MyUserDetailsServiceTest {
    @InjectMocks
    private MyUserDetailsService myUserDetailsService;
    @Mock
    private UserRepository userRepository;

    @Test
    public void loadUserByUsernameTest(){
        User user = new User("ravi","encodedPass",Role.EMPLOYEE);
        user.setId(4L);

        when(userRepository.loadUserByUsername("ravi")).thenReturn(user);

        UserDetails result = myUserDetailsService.loadUserByUsername("ravi");

        assertEquals("ravi",result.getUsername());
        assertEquals("encodedPass",result.getPassword());
        assertEquals("EMPLOYEE",result.getAuthorities().iterator().next().getAuthority());

        verify(userRepository,times(1)).loadUserByUsername("ravi");

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void loadUserByUsernameTest_Invalid(){
        when(userRepository.loadUserByUsername("nobody")).thenThrow(UsernameNotFoundException.class);

        assertThrows(UsernameNotFoundException.class,()->myUserDetailsService.loadUserByUsername("nobody"));

        verify(userRepository,times(1)).loadUserByUsername("nobody");

        verifyNoMoreInteractions(userRepository);
    }
}
