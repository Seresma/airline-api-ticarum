package com.airline.api.context;

import com.airline.api.auth.dto.SignupDto;
import com.airline.api.auth.services.UserServiceImpl;
import com.airline.api.dto.CreateFlightDto;
import com.airline.api.persistence.model.Airline;
import com.airline.api.persistence.model.Flight;
import com.airline.api.persistence.model.Plane;
import com.airline.api.services.AirlineServiceImpl;
import com.airline.api.utils.Utils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class TestDataLoaderConfig {

    //Only testing purpose
    @Bean
    CommandLineRunner initData(AirlineServiceImpl airlineService, UserServiceImpl userService) {
        if (GlobalConfig.IS_DATA_INITIALIZATION_ENABLE)
            return args -> {
                // Airline
                Airline airline = airlineService.createAirline(new Airline(Utils.capitalizeFirstLetter(GlobalConfig.AIRLINE_NAME), 5));
                // Planes
                airlineService.createPlane(new Plane(null, "Airbus A320", 250, airline, "EC-AA1"));
                airlineService.createPlane(new Plane(null, "Boeing 777", 500, airline, "EC-AA2"));
                airlineService.createPlane(new Plane(null, "Boeing 737", 300, airline, "EC-AA3"));
                airlineService.createPlane(new Plane(null, "Airbus A320", 250, airline, "EC-AA4"));
                airlineService.createPlane(new Plane(null, "Boeing 777", 500, airline, "EC-AA5"));
                //Flights
                airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA3"));
                airlineService.addFlight(new CreateFlightDto("Buenos Aires", "Paris", LocalDateTime.of(2023, 3, 29, 10, 0), LocalDateTime.of(2023, 3, 29, 20, 0), "EC-AA1"));
                //We suppose its 15/03 9:55 and these 2 flights have already departed
                Flight flightValencia = airlineService.addFlight(new CreateFlightDto("Valencia", "Barcelona", LocalDateTime.of(2023, 3, 15, 10, 0), LocalDateTime.of(2023, 3, 15, 12, 0), "EC-AA2"));
                Flight flightBerlin = airlineService.addFlight(new CreateFlightDto("Berlin", "Roma", LocalDateTime.of(2023, 3, 15, 10, 0), LocalDateTime.of(2023, 3, 15, 13, 0), "EC-AA5"));
                //Depart
                airlineService.departFlight(flightValencia.getId());
                airlineService.departFlight(flightBerlin.getId());
                // Users
                userService.registerUser(new SignupDto("user", "password", "user@gmail.com", "USER"));
                userService.registerUser(new SignupDto("admin", "password", "admin@gmail.com", "ADMIN"));
            };
        return null;
    }
}
