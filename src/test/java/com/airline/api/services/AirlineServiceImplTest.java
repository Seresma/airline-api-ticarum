package com.airline.api.services;

import com.airline.api.context.GlobalConfig;
import com.airline.api.dto.CreateFlightDto;
import com.airline.api.dto.FlightStatusDto;
import com.airline.api.dto.UpdateFlightDto;
import com.airline.api.exceptions.BadRequestException;
import com.airline.api.exceptions.DepartedFlightException;
import com.airline.api.exceptions.EntityNotFoundException;
import com.airline.api.persistence.model.Airline;
import com.airline.api.persistence.model.Flight;
import com.airline.api.persistence.model.FlightStatusEnum;
import com.airline.api.persistence.model.Plane;
import com.airline.api.persistence.repositories.AirlineRepository;
import com.airline.api.persistence.repositories.FlightRepository;
import com.airline.api.persistence.repositories.FlightStatusRepository;
import com.airline.api.persistence.repositories.PlaneRepository;
import com.airline.api.utils.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AirlineServiceImplTest {

    // !!! IMPORTANT -> GlobalConfig.IS_DATA_INITIALIZATION_ENABLE must be FALSE
    @Autowired
    private AirlineRepository airlineRepository;
    @Autowired
    private PlaneRepository planeRepository;
    @Autowired
    private FlightRepository flightRepository;
    @Autowired
    private FlightStatusRepository flightStatusRepository;
    @Autowired
    private AirlineServiceImpl airlineService;
    private final Airline airline = new Airline(Utils.capitalizeFirstLetter(GlobalConfig.AIRLINE_NAME), 5);
    private final Plane plane1 = new Plane(null, "Airbus A320", 250, null, "EC-AA1");

    private void tearDown() {
        this.flightRepository.deleteAll();
        this.flightStatusRepository.deleteAll();
        this.planeRepository.deleteAll();
        this.airlineRepository.deleteAll();
    }

    @Test
    public void whenGetAirlineInfoAirlineNotFound_thenEntityNotFoundException() throws EntityNotFoundException {
        Throwable exception = assertThrows(EntityNotFoundException.class, () -> this.airlineService.getAirline());
        assertEquals("Cannot find airline with name: " + GlobalConfig.AIRLINE_NAME, exception.getMessage());
    }

    @Test
    public void whenGetAirlineInfoOk_thenReturnAirline() {
        this.airlineRepository.save(this.airline);

        Airline airlineSaved = this.airlineService.getAirline();
        assertNotNull(airlineSaved.getId());
        assertEquals(Utils.capitalizeFirstLetter(GlobalConfig.AIRLINE_NAME), airlineSaved.getName());
        assertEquals(5, airlineSaved.getPlaneCount());
        assertEquals(0, airlineSaved.getPendingFlights().size());
        assertEquals(0, airlineSaved.getDepartedFlights().size());

        this.airlineRepository.deleteAll();
    }

    @Test
    public void whenGetPendingFlightsAirlineNotFound_thenEntityNotFoundException() throws EntityNotFoundException {
        Throwable exception = assertThrows(EntityNotFoundException.class, () -> this.airlineService.getPendingFlights());
        assertEquals("Cannot find airline with name: " + GlobalConfig.AIRLINE_NAME, exception.getMessage());
    }

    @Test
    public void whenGetPendingFlightsOk_thenReturnEmptySet() {
        this.airlineRepository.save(this.airline);

        assertEquals(0, this.airlineService.getPendingFlights().size());
        assertEquals(this.airlineService.getAirline().getPendingFlights().size(), this.airlineService.getPendingFlights().size());

        this.airlineRepository.deleteAll();
    }

    @Test
    public void whenAddFlightEtaBeforeEtd_thenBadRequestException() throws BadRequestException {
        this.airlineRepository.save(this.airline);

        Throwable exception = assertThrows(BadRequestException.class, () -> this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 8, 0), "EC-AA3")));
        assertEquals("Estimated date of departure (etd) must be before the estimated date of arrival (eta)", exception.getMessage());
        assertEquals(0, this.airlineService.getPendingFlights().size());

        this.airlineRepository.deleteAll();
    }

    @Test
    public void whenAddFlightEtaEqualEtd_thenBadRequestException() throws BadRequestException {
        this.airlineRepository.save(this.airline);

        Throwable exception = assertThrows(BadRequestException.class, () -> this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 10, 0), "EC-AA3")));
        assertEquals("Estimated date of departure (etd) must be before the estimated date of arrival (eta)", exception.getMessage());
        assertEquals(0, this.airlineService.getPendingFlights().size());

        this.airlineRepository.deleteAll();
    }

    @Test
    public void whenAddFlightPlaneRegistrationCodeDoesNotReferPlane_thenEntityNotFoundException() throws EntityNotFoundException {
        this.airlineRepository.save(this.airline);

        Throwable exception = assertThrows(EntityNotFoundException.class, () -> this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1")));
        assertEquals("Cannot find plane with registration code: EC-AA1", exception.getMessage());
        assertEquals(0, this.airlineService.getPendingFlights().size());

        this.airlineRepository.deleteAll();
    }

    @Test
    public void whenAddFlightNoAirline_thenEntityNotFoundException() throws EntityNotFoundException {
        this.airlineRepository.save(this.airline);
        this.planeRepository.save(plane1);

        Throwable exception = assertThrows(EntityNotFoundException.class, () -> this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1")));
        assertEquals("Cannot find the flight airline from the plane", exception.getMessage());
        assertEquals(0, this.airlineService.getPendingFlights().size());

        this.planeRepository.deleteAll();
        this.airlineRepository.deleteAll();
    }

    @Test
    public void whenAddFlightOk_thenReturnNewFlight() {
        Airline flightAirline = this.airlineRepository.save(this.airline);
        plane1.setAirline(flightAirline);
        Plane flightPlane = this.planeRepository.save(plane1);

        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        assertEquals("Murcia", flightCreated.getOrigin());
        assertEquals("Madrid", flightCreated.getDestination());
        assertEquals(LocalDateTime.of(2023, 3, 21, 10, 0), flightCreated.getEtd());
        assertEquals(LocalDateTime.of(2023, 3, 21, 12, 0), flightCreated.getEta());
        assertFalse(flightCreated.getHasDeparted());
        assertNull(flightCreated.getDepartDate());
        assertEquals(flightAirline, flightCreated.getAirline());
        assertEquals(flightPlane, flightCreated.getPlane());
        assertEquals(1, flightCreated.getStatuses().size());
        assertEquals(FlightStatusEnum.PENDING, flightCreated.getStatuses().get(0).getStatus());
        assertEquals(1, this.airlineService.getPendingFlights().size());
        assertTrue(this.airlineService.getPendingFlights().contains(flightCreated));

        this.tearDown();
    }

    @Test
    public void whenFindFlightByIdFlightNotFound_thenEntityNotFoundException() throws EntityNotFoundException {
        Throwable exception = assertThrows(EntityNotFoundException.class, () -> this.airlineService.findFlightById(12343214L));
        assertEquals("Cannot find flight with ID: " + 12343214L, exception.getMessage());
    }

    @Test
    public void whenFindFlightByIdOk_thenReturnFlight() {
        Airline flightAirline = this.airlineRepository.save(this.airline);
        plane1.setAirline(flightAirline);
        this.planeRepository.save(plane1);

        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        Flight flightReturned = this.airlineService.findFlightById(flightCreated.getId());
        assertEquals(flightReturned, flightCreated);

        this.tearDown();
    }

    @Test
    public void whenUpdateFlightByIdFlightNotFound_thenEntityNotFoundException() throws EntityNotFoundException {
        Throwable exception = assertThrows(EntityNotFoundException.class, () -> this.airlineService.updateFlightById(12343214L, new UpdateFlightDto()));
        assertEquals("Cannot find flight with ID: " + 12343214L, exception.getMessage());
    }

    @Test
    public void whenUpdateFlightByIdBlankOrigin_thenBadRequestException() throws BadRequestException {
        Airline flightAirline = this.airlineRepository.save(this.airline);
        plane1.setAirline(flightAirline);
        this.planeRepository.save(plane1);

        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        UpdateFlightDto updateFlightDto = new UpdateFlightDto("Alicante", "Barcelona", LocalDateTime.of(2023, 3, 21, 20, 0), LocalDateTime.of(2023, 3, 21, 22, 0), "EC-AA1");
        updateFlightDto.setOrigin(" ");
        Throwable exception = assertThrows(BadRequestException.class, () -> this.airlineService.updateFlightById(flightCreated.getId(), updateFlightDto));
        assertEquals("origin cannot be blank", exception.getMessage());
        assertEquals(flightCreated, this.airlineService.findFlightById(flightCreated.getId()));

        this.tearDown();
    }

    @Test
    public void whenUpdateFlightByIdBlankDestination_thenBadRequestException() throws BadRequestException {
        Airline flightAirline = this.airlineRepository.save(this.airline);
        plane1.setAirline(flightAirline);
        this.planeRepository.save(plane1);

        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        UpdateFlightDto updateFlightDto = new UpdateFlightDto("Alicante", "Barcelona", LocalDateTime.of(2023, 3, 21, 20, 0), LocalDateTime.of(2023, 3, 21, 22, 0), "EC-AA1");
        updateFlightDto.setDestination(" ");
        Throwable exception = assertThrows(BadRequestException.class, () -> this.airlineService.updateFlightById(flightCreated.getId(), updateFlightDto));
        assertEquals("destination cannot be blank", exception.getMessage());
        assertEquals(flightCreated, this.airlineService.findFlightById(flightCreated.getId()));

        this.tearDown();
    }

    @Test
    public void whenUpdateFlightByIdEtaBeforeEtd_thenBadRequestException() throws BadRequestException {
        Airline flightAirline = this.airlineRepository.save(this.airline);
        plane1.setAirline(flightAirline);
        this.planeRepository.save(plane1);

        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        UpdateFlightDto updateFlightDto = new UpdateFlightDto("Alicante", "Barcelona", LocalDateTime.of(2023, 3, 21, 20, 0), LocalDateTime.of(2023, 3, 21, 22, 0), "EC-AA1");
        updateFlightDto.setEta(LocalDateTime.of(2020, 3, 21, 20, 0));
        Throwable exception = assertThrows(BadRequestException.class, () -> this.airlineService.updateFlightById(flightCreated.getId(), updateFlightDto));
        assertEquals("Estimated date of departure (etd) must be before the estimated date of arrival (eta)", exception.getMessage());
        assertEquals(flightCreated, this.airlineService.findFlightById(flightCreated.getId()));

        this.tearDown();
    }

    @Test
    public void whenUpdateFlightByIdRegistrationCodeDoesNotReferPlane_thenEntityNotFoundException() throws EntityNotFoundException {
        Airline flightAirline = this.airlineRepository.save(this.airline);
        plane1.setAirline(flightAirline);
        this.planeRepository.save(plane1);

        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        UpdateFlightDto updateFlightDto = new UpdateFlightDto("Alicante", "Barcelona", LocalDateTime.of(2023, 3, 21, 20, 0), LocalDateTime.of(2023, 3, 21, 22, 0), "EG-AAH");
        Throwable exception = assertThrows(EntityNotFoundException.class, () -> this.airlineService.updateFlightById(flightCreated.getId(), updateFlightDto));
        assertEquals("Cannot find plane with registration code: EG-AAH", exception.getMessage());
        assertEquals(flightCreated, this.airlineService.findFlightById(flightCreated.getId()));

        this.tearDown();
    }

    @Test
    public void whenUpdateFlightByIdPlaneDoesNotReferAirline_thenEntityNotFoundException() throws EntityNotFoundException {
        Airline flightAirline = this.airlineRepository.save(this.airline);
        plane1.setAirline(flightAirline);
        this.planeRepository.save(plane1);

        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        UpdateFlightDto updateFlightDto = new UpdateFlightDto("Alicante", "Barcelona", LocalDateTime.of(2023, 3, 21, 20, 0), LocalDateTime.of(2023, 3, 21, 22, 0), "EC-AA1");
        plane1.setAirline(null);
        this.planeRepository.save(plane1);
        Throwable exception = assertThrows(EntityNotFoundException.class, () -> this.airlineService.updateFlightById(flightCreated.getId(), updateFlightDto));
        assertEquals("Cannot find the flight airline from the plane", exception.getMessage());
        assertEquals(flightCreated, this.airlineService.findFlightById(flightCreated.getId()));

        this.tearDown();
    }

    @Test
    public void whenUpdateFlightByIdAllNullProps_thenReturn() {
        Airline flightAirline = this.airlineRepository.save(this.airline);
        plane1.setAirline(flightAirline);
        this.planeRepository.save(plane1);

        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        UpdateFlightDto updateFlightDto = new UpdateFlightDto(null, null, null, null, null);
        this.airlineService.updateFlightById(flightCreated.getId(), updateFlightDto);
        assertEquals(flightCreated, this.airlineService.findFlightById(flightCreated.getId()));

        this.tearDown();
    }

    @Test
    public void whenUpdateFlightByIdOk_thenUpdate() {
        Airline flightAirline = this.airlineRepository.save(this.airline);
        plane1.setAirline(flightAirline);
        this.planeRepository.save(plane1);
        Plane plane2 = this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA2"));

        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        UpdateFlightDto updateFlightDto = new UpdateFlightDto("Alicante", "Barcelona", LocalDateTime.of(2023, 3, 21, 20, 0), LocalDateTime.of(2023, 3, 21, 22, 0), "EC-AA2");
        this.airlineService.updateFlightById(flightCreated.getId(), updateFlightDto);
        Flight flightUpdated = this.airlineService.findFlightById(flightCreated.getId());
        assertNotSame(flightCreated, flightUpdated);
        assertEquals(flightCreated.getId(), flightUpdated.getId());
        assertEquals("Alicante", flightUpdated.getOrigin());
        assertEquals("Barcelona", flightUpdated.getDestination());
        assertEquals(LocalDateTime.of(2023, 3, 21, 20, 0), flightUpdated.getEtd());
        assertEquals(LocalDateTime.of(2023, 3, 21, 22, 0), flightUpdated.getEta());
        assertEquals(plane2, flightUpdated.getPlane());
        assertEquals(flightAirline, flightUpdated.getAirline());
        assertNull(flightUpdated.getDepartDate());
        assertFalse(flightUpdated.getHasDeparted());

        this.tearDown();
    }

    @Test
    public void whenDeleteFlightByIdFlightNotFound_thenEntityNotFoundException() throws EntityNotFoundException {
        Throwable exception = assertThrows(EntityNotFoundException.class, () -> this.airlineService.deleteFlightById(12343214L));
        assertEquals("Cannot find flight with ID: " + 12343214L, exception.getMessage());
    }

    @Test
    public void whenDeleteFlightByIdOk_thenDelete() {
        Airline flightAirline = this.airlineService.createAirline(this.airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));

        this.airlineService.deleteFlightById(flightCreated.getId());

        assertEquals(Optional.empty(), this.flightRepository.findById(flightCreated.getId()));
        assertEquals(0, this.airlineService.getAirline().getPendingFlights().size());

        tearDown();
    }

    @Test
    public void whenGetDepartedFlightsAirlineNotFound_thenEntityNotFoundException() throws EntityNotFoundException {
        Throwable exception = assertThrows(EntityNotFoundException.class, () -> this.airlineService.getDepartedFlights());
        assertEquals("Cannot find airline with name: " + GlobalConfig.AIRLINE_NAME, exception.getMessage());
    }

    @Test
    public void whenGetDepartedFlightsOk_thenReturnEmptySet() {
        this.airlineRepository.save(this.airline);

        assertEquals(0, this.airlineService.getDepartedFlights().size());
        assertEquals(this.airlineService.getAirline().getDepartedFlights().size(), this.airlineService.getDepartedFlights().size());

        this.airlineRepository.deleteAll();
    }

    @Test
    public void whenGetFlightStatusFlightNotFound_thenEntityNotFoundException() throws EntityNotFoundException {
        Throwable exception = assertThrows(EntityNotFoundException.class, () -> this.airlineService.getFlightStatus(12343214L));
        assertEquals("Cannot find flight with ID: " + 12343214L, exception.getMessage());
    }

    @Test
    public void whenGetFlightStatusPendingFlight_thenReturnFalseNullDto() {
        Airline flightAirline = this.airlineService.createAirline(this.airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));

        FlightStatusDto flightStatusDto = this.airlineService.getFlightStatus(flightCreated.getId());
        assertFalse(flightStatusDto.getHasDeparted());
        assertNull(flightStatusDto.getDepartDate());

        tearDown();
    }

    @Test
    public void whenDepartFlightOk_thenDepart() throws DepartedFlightException {
        Airline flightAirline = this.airlineService.createAirline(this.airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));

        this.airlineService.departFlight(flightCreated.getId());
        Flight flightDeparted = this.airlineService.findFlightById(flightCreated.getId());
        assertTrue(flightDeparted.getHasDeparted());
        assertNotNull(flightDeparted.getDepartDate());
        assertEquals(1, this.airlineService.getDepartedFlights().size());
        assertTrue(this.airlineService.getDepartedFlights().contains(flightDeparted));
        assertEquals(0, this.airlineService.getPendingFlights().size());

        tearDown();
    }

    @Test
    public void whenDepartDepartedFlight_thenDepartedFlightException() throws DepartedFlightException {
        Airline flightAirline = this.airlineService.createAirline(this.airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.airlineService.departFlight(flightCreated.getId());

        Throwable exception = assertThrows(DepartedFlightException.class, () -> this.airlineService.departFlight(flightCreated.getId()));
        assertEquals("Flight with ID: " + flightCreated.getId() + " has already departed", exception.getMessage());

        tearDown();
    }

    @Test
    public void whenGetFlightStatusDepartedFlight_thenReturnFalseNullDto() {
        Airline flightAirline = this.airlineService.createAirline(this.airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        this.airlineService.departFlight(flightCreated.getId());

        FlightStatusDto flightStatusDto = this.airlineService.getFlightStatus(flightCreated.getId());
        assertTrue(flightStatusDto.getHasDeparted());
        assertNotNull(flightStatusDto.getDepartDate());

        tearDown();
    }

    @Test
    public void whenGetPendingFlightsOk_thenReturnSet() {
        Airline flightAirline = this.airlineService.createAirline(this.airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        Flight flightCreated1 = this.airlineService.addFlight(new CreateFlightDto("Berlin", "Barcelona", LocalDateTime.of(2023, 4, 21, 20, 0), LocalDateTime.of(2023, 4, 21, 22, 0), "EC-AA1"));
        Flight flightCreated2 = this.airlineService.addFlight(new CreateFlightDto("Miami", "Paris", LocalDateTime.of(2023, 5, 21, 8, 0), LocalDateTime.of(2023, 5, 21, 15, 0), "EC-AA1"));

        assertEquals(3, this.airlineService.getPendingFlights().size());
        assertTrue(this.airlineService.getPendingFlights().containsAll(Arrays.asList(flightCreated, flightCreated1, flightCreated2)));
        assertEquals(this.airlineService.getAirline().getPendingFlights().size(), this.airlineService.getPendingFlights().size());

        tearDown();
    }

    @Test
    public void whenGetDepartedFlightsOk_thenReturnSet() {
        Airline flightAirline = this.airlineService.createAirline(this.airline);
        this.airlineService.createPlane(new Plane(null, "Boeing 777", 500, flightAirline, "EC-AA1"));
        Flight flightCreated = this.airlineService.addFlight(new CreateFlightDto("Murcia", "Madrid", LocalDateTime.of(2023, 3, 21, 10, 0), LocalDateTime.of(2023, 3, 21, 12, 0), "EC-AA1"));
        Flight flightCreated1 = this.airlineService.addFlight(new CreateFlightDto("Berlin", "Barcelona", LocalDateTime.of(2023, 4, 21, 20, 0), LocalDateTime.of(2023, 4, 21, 22, 0), "EC-AA1"));
        Flight flightCreated2 = this.airlineService.addFlight(new CreateFlightDto("Miami", "Paris", LocalDateTime.of(2023, 5, 21, 8, 0), LocalDateTime.of(2023, 5, 21, 15, 0), "EC-AA1"));
        this.airlineService.departFlight(flightCreated.getId());
        this.airlineService.departFlight(flightCreated1.getId());
        this.airlineService.departFlight(flightCreated2.getId());

        assertEquals(3, this.airlineService.getDepartedFlights().size());
        assertTrue(this.airlineService.getDepartedFlights().containsAll(Arrays.asList(flightCreated, flightCreated1, flightCreated2)));
        assertEquals(this.airlineService.getAirline().getDepartedFlights().size(), this.airlineService.getDepartedFlights().size());

        tearDown();
    }

}
