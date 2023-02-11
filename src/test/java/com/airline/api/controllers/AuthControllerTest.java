package com.airline.api.controllers;

import com.airline.api.auth.repositories.UserRepository;
import com.airline.api.auth.services.UserServiceImpl;
import com.airline.api.context.GlobalConfig;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    // !!! IMPORTANT -> GlobalConfig.IS_AUTHENTICATION_ENABLE must be FALSE
    @Autowired
    private MockMvc mockMvcAuthController;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private UserRepository userRepository;

    private final String baseUri = "/" + GlobalConfig.AIRLINE_NAME;

    @After
    public void tearDown() {
        this.userRepository.deleteAll();
    }

    @Test
    public void whenRegisterUser_thenOk() throws Exception {
        this.mockMvcAuthController.perform(post(this.baseUri + "/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\"," +
                        "\"password\":\"password\"," +
                        "\"email\":\"user@gmail.com\"," +
                        "\"rol\":\"USER\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));
    }

}
