package com.airline.api.controllers;

import com.airline.api.context.GlobalConfig;
import com.airline.api.dto.CreateFlightDto;
import com.airline.api.persistence.model.Airline;
import com.airline.api.persistence.model.Flight;
import com.airline.api.persistence.model.Plane;
import com.airline.api.persistence.repositories.AirlineRepository;
import com.airline.api.persistence.repositories.FlightRepository;
import com.airline.api.persistence.repositories.FlightStatusRepository;
import com.airline.api.persistence.repositories.PlaneRepository;
import com.airline.api.services.AirlineServiceImpl;
import com.airline.api.utils.Utils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AirlineControllerTest {

    // !!! IMPORTANT -> GlobalConfig.IS_AUTHENTICATION_ENABLE and GlobalConfig.IS_DATA_INITIALIZATION_ENABLE both must be FALSE
    private final Airline airline = new Airline(Utils.capitalizeFirstLetter(GlobalConfig.AIRLINE_NAME), 5);
    private final String baseUri = "/" + GlobalConfig.AIRLINE_NAME;

    @Autowired
    private MockMvc mockMvcAirlineController;
    @Autowired
    private AirlineServiceImpl airlineService;
    @Autowired
    private AirlineRepository airlineRepository;
    @Autowired
    private PlaneRepository planeRepository;
    @Autowired
    private FlightRepository flightRepository;
    @Autowired
    private FlightStatusRepository flightStatusRepository;

    @After
    public void tearDown() {
        this.flightRepository.deleteAll();
        this.flightStatusRepository.deleteAll();
        this.planeRepository.deleteAll();
        this.airlineRepository.deleteAll();
    }

    @Test
    public void whenGetAirlineInfoNoAirline_thenNotFound() throws Exception {
        this.mockMvcAirlineController.perform(get(this.baseUri + "/info")).andExpect(status().isNotFound())
                .andExpect((jsonPath("$.message").value("Cannot find airline with name: " + GlobalConfig.AIRLINE_NAME)));
    }

    @Test
    public void whenGetAirlineInfoOk_thenReturnAirlineOk() throws Exception {
        this.airlineService.createAirline(airline);
        this.mockMvcAirlineController.perform(get(this.baseUri + "/info")).andExpectAll(status().isOk())
                .andExpect((jsonPath("$.id").exists()))
                .andExpect((jsonPath("$.name").value("Airline")))
                .andExpect((jsonPath("$.planeCount").value(5)));
    }

    @Test
    public void whenFindAllPendingFlightsNoAirline_thenNotFound() throws Exception {
        this.mockMvcAirlineController.perform(get(this.baseUri + "/vuelo")).andExpect(status().isNotFound())
                .andExpect((jsonPath("$.message").value("Cannot find airline with name: " + GlobalConfig.AIRLINE_NAME)));
    }

    @Test
    public void whenFindAllPendingFlightsOk_thenOk() throws Exception {
        this.airlineService.createAirline(airline);
        this.mockMvcAirlineController.perform(get(this.baseUri + "/vuelo")).andExpect(status().isOk());
    }

    @Test
    public void whenAddFlight_thenCreated() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        Plane plane1 = this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.mockMvcAirlineController.perform(post(this.baseUri + "/vuelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isCreated())
                .andExpect((jsonPath("$.id").exists()))
                .andExpect((jsonPath("$.origin").value("Miami")))
                .andExpect((jsonPath("$.destination").value("Paris")))
                .andExpect((jsonPath("$.etd").value("2023-03-06T17:34:24.443")))
                .andExpect((jsonPath("$.eta").value("2023-03-07T18:34:24.443")))
                .andExpect((jsonPath("$.hasDeparted").value(false)))
                .andExpect((jsonPath("$.plane").value(plane1)))
                .andExpect((jsonPath("$.statuses.size()").value(1)))
                .andExpect((jsonPath("$.statuses[0].status").value("PENDING")))
                .andExpect((jsonPath("$.statuses[0].statusDate").isNotEmpty()));
    }

    @Test
    public void whenAddFlightInvalidSchedule_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.mockMvcAirlineController.perform(post(this.baseUri + "/vuelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-07T19:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect((jsonPath("$.message").value("Estimated date of departure (etd) must be before the estimated date of arrival (eta)")));
    }

    @Test
    public void whenAddFlightPastEtd_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.mockMvcAirlineController.perform(post(this.baseUri + "/vuelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2022-03-07T19:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenAddFlightPastEta_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.mockMvcAirlineController.perform(post(this.baseUri + "/vuelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-07T19:34:24.443\"," +
                                "\"eta\":\"2022-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenAddFlightBlankProperty_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.mockMvcAirlineController.perform(post(this.baseUri + "/vuelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"  \"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenAddFlightBadOriginPattern_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.mockMvcAirlineController.perform(post(this.baseUri + "/vuelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"123442412\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenAddFlightBadDestinationPattern_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.mockMvcAirlineController.perform(post(this.baseUri + "/vuelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"1234123\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenAddFlightNullProperty_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.mockMvcAirlineController.perform(post(this.baseUri + "/vuelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenAddFlightBadRegistrationCodeFormat_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.mockMvcAirlineController.perform(post(this.baseUri + "/vuelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"ECA12345\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenAddFlightRegistrationCodeNoPlane_thenNotFound() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.mockMvcAirlineController.perform(post(this.baseUri + "/vuelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-ABC\"}"))
                .andExpect(status().isNotFound())
                .andExpect((jsonPath("$.message").value("Cannot find plane with registration code: EC-ABC")));
    }

    @Test
    public void whenGetFlightByIdNoFlight_thenNotFound() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.mockMvcAirlineController.perform(get(this.baseUri + "/vuelo" + "/20"))
                .andExpect(status().isNotFound())
                .andExpect((jsonPath("$.message").value("Cannot find flight with ID: 20")));
    }

    @Test
    public void whenGetFlightByIdInvalidId_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.mockMvcAirlineController.perform(get(this.baseUri + "/vuelo" + "/gsdfgsdgf"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenGetFlightByIdOk_thenOk() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(get(this.baseUri + "/vuelo" + "/" + flightCreated.getId()))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$.id").value(flightCreated.getId())))
                .andExpect((jsonPath("$.origin").value(flightCreated.getOrigin())))
                .andExpect((jsonPath("$.destination").value(flightCreated.getDestination())))
                .andExpect((jsonPath("$.etd").value("2023-03-21T10:00:00")))
                .andExpect((jsonPath("$.eta").value("2023-03-21T12:00:00")))
                .andExpect((jsonPath("$.hasDeparted").value(flightCreated.getHasDeparted())))
                .andExpect((jsonPath("$.plane").value(flightCreated.getPlane())))
                .andExpect((jsonPath("$.statuses.size()").value(flightCreated.getStatuses().size())))
                .andExpect((jsonPath("$.statuses[0].status").value(flightCreated.getStatuses().get(0).getStatus().toString())))
                .andExpect((jsonPath("$.statuses[0].statusDate").isNotEmpty()));
    }

    @Test
    public void whenUpdateFlightByIdOk_thenNoContent() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.airlineService.createPlane(new Plane(null, "Airbus A320", 250, flightAirline, "EC-AA2"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/vuelo" + "/" + flightCreated.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA2\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void whenUpdateFlightByIdNullProps_thenNoContent() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.airlineService.createPlane(new Plane(null, "Airbus A320", 250, flightAirline, "EC-AA2"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/vuelo" + "/" + flightCreated.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void whenUpdateFlightByIdInvalidId_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.airlineService.createPlane(new Plane(null, "Airbus A320", 250, flightAirline, "EC-AA2"));
        this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/vuelo" + "/dasdfasfd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA2\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenUpdateFlightByIdBlankProperty_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.airlineService.createPlane(new Plane(null, "Airbus A320", 250, flightAirline, "EC-AA2"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/vuelo" + "/" + flightCreated.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"  \"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA2\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenUpdateFlightBadOriginPattern_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/vuelo" + "/" + flightCreated.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"123442412\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenUpdateFlightBadDestinationPattern_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/vuelo" + "/" + flightCreated.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"1234123\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenUpdateFlightBadRegistrationCodeFormat_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/vuelo" + "/" + flightCreated.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"ECA12345\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenUpdateFlightRegistrationCodeNoPlane_thenNotFound() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/vuelo" + "/" + flightCreated.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-06T17:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-ABC\"}"))
                .andExpect(status().isNotFound())
                .andExpect((jsonPath("$.message").value("Cannot find plane with registration code: EC-ABC")));
    }

    @Test
    public void whenUpdateFlightInvalidSchedule_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/vuelo" + "/" + flightCreated.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-07T19:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect((jsonPath("$.message").value("Estimated date of departure (etd) must be before the estimated date of arrival (eta)")));
    }

    @Test
    public void whenUpdateFlightPastEtd_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/vuelo" + "/" + flightCreated.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2022-03-07T19:34:24.443\"," +
                                "\"eta\":\"2023-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenUpdateFlightPastEta_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/vuelo" + "/" + flightCreated.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origin\":\"Miami\"," +
                                "\"destination\":\"Paris\"," +
                                "\"etd\":\"2023-03-07T19:34:24.443\"," +
                                "\"eta\":\"2022-03-07T18:34:24.443\"," +
                                "\"planeRegistrationCode\":\"EC-AA1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenDeleteFlightByIdInvalidId_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(delete(this.baseUri + "/vuelo" + "/adfasfsa"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenDeleteFlightNoFlight_thenNotFound() throws Exception {
        this.airlineService.createAirline(airline);
        this.mockMvcAirlineController.perform(delete(this.baseUri + "/vuelo" + "/20"))
                .andExpect(status().isNotFound())
                .andExpect((jsonPath("$.message").value("Cannot find flight with ID: 20")));
    }

    @Test
    public void whenDeleteFlightByIdOk_thenNoContent() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(delete(this.baseUri + "/vuelo" + "/" + flightCreated.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void whenFindAllDepartedFlightsNoAirline_thenNotFound() throws Exception {
        this.mockMvcAirlineController.perform(get(this.baseUri + "/salida")).andExpect(status().isNotFound())
                .andExpect((jsonPath("$.message").value("Cannot find airline with name: " + GlobalConfig.AIRLINE_NAME)));
    }

    @Test
    public void whenFindAllDepartedFlightsOk_thenOk() throws Exception {
        this.airlineService.createAirline(airline);
        this.mockMvcAirlineController.perform(get(this.baseUri + "/salida")).andExpect(status().isOk());
    }

    @Test
    public void whenGetFlightStatusPendingFlightOk_thenOk() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(get(this.baseUri + "/salida" + "/" + flightCreated.getId()))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$.hasDeparted").value(false)))
                .andExpect((jsonPath("$.departDate").doesNotExist()));
    }

    @Test
    public void whenGetFlightStatusDepartedFlightOk_thenOk() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.airlineService.departFlight(flightCreated.getId());
        this.mockMvcAirlineController.perform(get(this.baseUri + "/salida" + "/" + flightCreated.getId()))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$.hasDeparted").value(true)))
                .andExpect((jsonPath("$.departDate").isNotEmpty()));
    }

    @Test
    public void whenGetFlightStatusInvalidId_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(get(this.baseUri + "/salida" + "/adfasfsa"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenGetFlightStatusNoFlight_thenNotFound() throws Exception {
        this.airlineService.createAirline(airline);
        this.mockMvcAirlineController.perform(get(this.baseUri + "/salida" + "/20"))
                .andExpect(status().isNotFound())
                .andExpect((jsonPath("$.message").value("Cannot find flight with ID: 20")));
    }

    @Test
    public void whenDepartFlightNoFlight_thenNotFound() throws Exception {
        this.airlineService.createAirline(airline);
        this.mockMvcAirlineController.perform(put(this.baseUri + "/salida" + "/20" + "/despegue"))
                .andExpect(status().isNotFound())
                .andExpect((jsonPath("$.message").value("Cannot find flight with ID: 20")));
    }

    @Test
    public void whenDepartFlightInvalidId_thenBadRequest() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/salida" + "/adfasfsa" + "/despegue"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenDepartFlightOk_thenNoContent() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.mockMvcAirlineController.perform(put(this.baseUri + "/salida" + "/" + flightCreated.getId() + "/despegue"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void whenDepartFlightDepartedFlight_thenConflict() throws Exception {
        Airline flightAirline = this.airlineService.createAirline(airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.airlineService.departFlight(flightCreated.getId());
        this.mockMvcAirlineController.perform(put(this.baseUri + "/salida" + "/" + flightCreated.getId() + "/despegue"))
                .andExpect(status().isConflict())
                .andExpect((jsonPath("$.message").value("Flight with ID: " + flightCreated.getId() + " has already departed")));
    }
}
