package com.airline.api;

import com.airline.api.services.AirlineService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AirlineApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirlineApplication.class, args);
	}

	@Bean
	CommandLineRunner initData(AirlineService airlineService) {
		return args -> {
			// airlineService.createTask(new TaskInDTO("Task 1","Study", LocalDateTime.now()));
		};
	}

}
