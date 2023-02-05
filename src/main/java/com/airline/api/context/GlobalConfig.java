package com.airline.api.context;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalConfig {
    public static final String AIRLINE_NAME = "MisakiAirlines";
    @Bean
    public ModelMapper modelMapper (){
        return new ModelMapper();
    }
}
