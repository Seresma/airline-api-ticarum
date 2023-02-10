package com.airline.api.auth.services;

import com.airline.api.auth.dto.SignupDto;
import com.airline.api.auth.model.Role;
import com.airline.api.auth.model.User;
import com.airline.api.auth.repositories.UserRepository;
import com.airline.api.exceptions.BadRequestException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceImplTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserServiceImpl userService;

    @After
    public void tearDown() {
        this.userRepository.deleteAll();
    }

    @Test
    public void whenRegisterUser_thenReturnCreated() {
        this.userService.registerUser(new SignupDto("user", "password", "user@gmail.com", "USER"));
        User user = this.userRepository.findByUsername("user");

        assertNotNull(user.getId());
        assertEquals("user", user.getUsername());
        // Encoded password
        assertNotNull(user.getPassword());
        assertEquals("user@gmail.com", user.getEmail());
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    public void whenRegisterUserUsernameAlreadyExists_thenBadRequestException() throws BadRequestException {
        this.userService.registerUser(new SignupDto("user", "password", "user@gmail.com", "USER"));
        Throwable exception = assertThrows(BadRequestException.class, () -> this.userService.registerUser(new SignupDto("user", "password", "user@gmail.com", "USER")));
        assertEquals("There is already a user with username: user", exception.getMessage());
    }

}
