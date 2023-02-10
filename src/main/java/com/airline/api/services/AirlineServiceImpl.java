package com.airline.api.services;

import com.airline.api.context.GlobalConfig;
import com.airline.api.dto.CreateFlightDto;
import com.airline.api.dto.FlightStatusDto;
import com.airline.api.dto.UpdateFlightDto;
import com.airline.api.exceptions.BadRequestException;
import com.airline.api.exceptions.DepartedFlightException;
import com.airline.api.exceptions.EntityNotFoundException;
import com.airline.api.persistence.model.*;
import com.airline.api.persistence.repositories.AirlineRepository;
import com.airline.api.persistence.repositories.FlightRepository;
import com.airline.api.persistence.repositories.FlightStatusRepository;
import com.airline.api.persistence.repositories.PlaneRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@Service
public class AirlineServiceImpl {
    private final AirlineRepository airlineRepository;
    private final PlaneRepository planeRepository;
    private final FlightRepository flightRepository;
    private final FlightStatusRepository flightStatusRepository;
    private final ModelMapper modelmapper;

    private FlightStatus createFlightStatus(LocalDateTime dateTime, FlightStatusEnum flightStatusEnum) {
        return this.flightStatusRepository.save(new FlightStatus(null, dateTime, flightStatusEnum));
    }

    //Only testing purpose
    public Airline createAirline(Airline airline) {
        return this.airlineRepository.save(airline);
    }

    //Only testing purpose
    public Plane createPlane(Plane plane) {
        return this.planeRepository.save(plane);
    }

    public Airline getAirline() {
        Airline airline = this.airlineRepository.findByNameIgnoreCase(GlobalConfig.AIRLINE_NAME);
        if (airline == null)
            throw new EntityNotFoundException("Cannot find airline with name: " + GlobalConfig.AIRLINE_NAME);
        return airline;
    }

    public Set<Flight> getPendingFlights() {
        return this.getAirline().getPendingFlights();
    }

    public Flight addFlight(CreateFlightDto flightDTO) {
        Flight flight = modelmapper.map(flightDTO, Flight.class);
        if (!flight.isCorrectSchedule())
            throw new BadRequestException("Estimated date of departure (etd) must be before the estimated date of arrival (eta)");

        Plane plane = this.planeRepository.findByRegistrationCode(flightDTO.getPlaneRegistrationCode());
        if (plane == null)
            throw new EntityNotFoundException("Cannot find plane with registration code: " + flightDTO.getPlaneRegistrationCode());

        flight.setPlane(plane);

        Airline airline = flight.getAirline();
        if (airline == null)
            throw new EntityNotFoundException("Cannot find the flight airline from the plane");

        flight.setHasDeparted(false);
        flight.addFlightStatus(this.createFlightStatus(LocalDateTime.now(), FlightStatusEnum.PENDING));
        Flight flightCreated = this.flightRepository.save(flight);
        airline.addPendingFlight(flightCreated);
        this.airlineRepository.save(airline);

        return flightCreated;
    }

    public Flight findFlightById(Long id) {
        return this.flightRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find flight with ID: " + id));
    }

    @Transactional
    public void updateFlightById(Long id, UpdateFlightDto flightDTO) {
        Flight flight = this.findFlightById(id);
        String originUpdate = flightDTO.getOrigin();
        if (originUpdate != null) {
            if (originUpdate.isBlank())
                throw new BadRequestException("origin cannot be blank");

            flight.setOrigin(originUpdate);
        }

        String destinationUpdate = flightDTO.getDestination();
        if(destinationUpdate != null) {
            if(destinationUpdate.isBlank())
                throw new BadRequestException("destination cannot be blank");

            flight.setDestination(destinationUpdate);
        }

        LocalDateTime etdUpdate = flightDTO.getEtd();
        if(etdUpdate != null) {
            flight.setEtd(etdUpdate);
        }

        LocalDateTime etaUpdate = flightDTO.getEta();
        if (etaUpdate != null) {
            flight.setEta(etaUpdate);
            if (!flight.isCorrectSchedule())
                throw new BadRequestException("Estimated date of departure (etd) must be before the estimated date of arrival (eta)");
        }

        String registrationCodeUpdate = flightDTO.getPlaneRegistrationCode();
        if (registrationCodeUpdate != null) {
            Plane plane = this.planeRepository.findByRegistrationCode(registrationCodeUpdate);
            if (plane == null)
                throw new EntityNotFoundException("Cannot find plane with registration code: " + flightDTO.getPlaneRegistrationCode());
            flight.setPlane(plane);

            if (flight.getAirline() == null)
                throw new EntityNotFoundException("Cannot find the flight airline from the plane");
        }

        this.flightRepository.save(flight);
    }

    public void deleteFlightById(Long id) {
        Flight flight = this.findFlightById(id);
        // Flight must have an airline, cannot be null
        Airline airline = flight.getAirline();

        if (airline.isInPendingFlights(flight))
            airline.removePendingFlight(flight);
        else
            airline.removeDepartedFlight(flight);

        this.airlineRepository.save(airline);
        this.flightRepository.deleteById(id);
    }

    public Set<Flight> getDepartedFlights() {
        return this.getAirline().getDepartedFlights();
    }

    public FlightStatusDto getFlightStatus(Long id) {
        Flight flight = this.findFlightById(id);
        if (flight.getHasDeparted()) {
            return modelmapper.map(flight, FlightStatusDto.class);
        }
        return new FlightStatusDto(false, null);
    }

    @Transactional
    public void departFlight(Long id) {
        Flight flight = this.findFlightById(id);
        // Flight must have an airline, cannot be null
        Airline airline = flight.getAirline();

        if (flight.getHasDeparted())
            throw new DepartedFlightException(flight.getId());

        flight.setHasDeparted(true);
        flight.addFlightStatus(this.createFlightStatus(LocalDateTime.now(), FlightStatusEnum.DEPARTED));
        flight.setDepartDate(LocalDateTime.now());
        airline.addDepartedFlight(flight);

        this.flightRepository.save(flight);
        this.airlineRepository.save(airline);
    }
}
