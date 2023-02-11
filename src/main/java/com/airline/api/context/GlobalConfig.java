package com.airline.api.context;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalConfig {

    public static final String AIRLINE_NAME = "airline";
    // To run tests this must be false
    public static final boolean IS_AUTHENTICATION_ENABLE = false;
    // To run tests this must be false
    public static final boolean IS_DATA_INITIALIZATION_ENABLE = false;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
