package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.response.DetailsDto;
import com.bank.MaverickBank.enums.Role;
import com.bank.MaverickBank.exception.InactiveUserException;
import com.bank.MaverickBank.model.User;
import com.bank.MaverickBank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserObject(String username, String password, Role role){
        log.info("Initializing addition of User to The Database");
        User user=new User(username,
                passwordEncoder.encode(password),
                            role );
        log.info("Completing addition of User to The Database");
        return userRepository.save(user);
    }

    public DetailsDto getUserDetails(String loggedInUsername) {
        DetailsDto detailsDto = userRepository.getUserDetails(loggedInUsername);
        if(!detailsDto.isActive())
            throw new InactiveUserException("User is disabled");
        return  detailsDto;
    }
}
