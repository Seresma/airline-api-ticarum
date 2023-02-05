package com.airline.api.services;

import com.airline.api.context.GlobalConfig;
import com.airline.api.dto.FlightDTO;
import com.airline.api.persistence.domain.*;
import com.airline.api.persistence.repositories.IFlightRepository;
import com.airline.api.persistence.repositories.IAirlineRepository;
import com.airline.api.persistence.repositories.IFlightStatusRepository;
import com.airline.api.persistence.repositories.IPlaneRepository;
import com.airline.api.responses.FlightStatusResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@Service
public class AirlineService {
    private final IAirlineRepository airlineRepository;
    private final IPlaneRepository planeRepository;
    private final IFlightRepository flightRepository;
    private final IFlightStatusRepository flightStatusRepository;
    private final ModelMapper modelmapper;

    private Airline getAirline() {
        return this.airlineRepository.findByNameIgnoreCase(GlobalConfig.AIRLINE_NAME);
    }

    private FlightStatus addFlightStatus(LocalDateTime dateTime, FlightStatusEnum flightStatusEnum) {
        return this.flightStatusRepository.save(new FlightStatus(null, dateTime, flightStatusEnum));
    }

    public Airline getInfo() {
        return this.airlineRepository.findByNameIgnoreCase(GlobalConfig.AIRLINE_NAME);
    }

    public Set<Flight> getPendingFlights() {
        return this.getAirline().getPendingFlights();
    }

    public Flight addFlight(FlightDTO flightDTO) {
        Flight flight = modelmapper.map(flightDTO, Flight.class);
        // TODO PUEDE FALLAR
        Plane plane = this.planeRepository.findByRegistrationCode(flightDTO.getPlaneRegistrationCode());
        flight.setPlane(plane);
        flight.setHasDeparted(false);
        flight.addFlightStatus(this.addFlightStatus(LocalDateTime.now(), FlightStatusEnum.PENDING));
        Airline airline = this.getAirline();
        flight.setAirline(airline);
        if(!airline.isInDepartedFlights(flight) && airline.addPendingFlight(flight)) {
            return this.flightRepository.save(flight);
        }
        return null;
    }

    public Flight findFlightById(Long id) {
        // TODO PUEDE FALLAR
        return this.flightRepository.findById(id).orElse(null);
    }

    @Transactional
    public void updateFlightById(Long id, FlightDTO flightDTO) {
        // TODO COMPROBACION DE ERRORES
        Flight flight = this.findFlightById(id);
        String numberUpdate = flightDTO.getNumber();
        if(numberUpdate != null && !numberUpdate.isBlank())
            flight.setNumber(numberUpdate);
        String originUpdate = flightDTO.getOrigin();
        if(originUpdate != null && !originUpdate.isBlank())
            flight.setOrigin(originUpdate);
        String destinationUpdate = flightDTO.getDestination();
        if(destinationUpdate != null && !destinationUpdate.isBlank())
            flight.setDestination(destinationUpdate);
        LocalDateTime etaUpdate = flightDTO.getEta();
        if(etaUpdate != null && etaUpdate.isAfter(LocalDateTime.now()))
            flight.setEtd(etaUpdate);
        LocalDateTime etdUpdate = flightDTO.getEtd();
        if(etdUpdate != null && etdUpdate.isAfter(LocalDateTime.now()) && etdUpdate.isBefore(flight.getEta()))
            flight.setEtd(etdUpdate);
        String registrationCodeUpdate = flightDTO.getPlaneRegistrationCode();
        if(registrationCodeUpdate != null && !registrationCodeUpdate.isBlank())
            flight.setPlane(this.planeRepository.findByRegistrationCode(registrationCodeUpdate));

        this.flightRepository.save(flight);
    }

    public void deleteFlightById(Long id) {
        Flight flight = this.findFlightById(id);
        // TODO PUEDE FALLAR
        if (this.getAirline().isInPendingFlights(flight))
            this.getAirline().removePendingFlight(flight);
        else
            this.getAirline().removeDepartedFlight(flight);

        this.airlineRepository.save(this.getAirline());
        this.flightRepository.deleteById(id);
    }

    public Set<Flight> getDepartedFlights() {
        return this.getAirline().getDepartedFlights();
    }

    public FlightStatusResponse getFlightStatus(Long id) {
        Flight flight = this.findFlightById(id);
        if(flight.getHasDeparted())
            return new FlightStatusResponse(flight.getHasDeparted(), flight.getLastFlightStatusDate());
        // TODO PUEDE FALLAR
        return new FlightStatusResponse(false, null);
    }

    @Transactional
    public void departFlight(Long id) {
        Flight flight = this.findFlightById(id);
        // TODO PUEDE FALLAR
        Airline airline = this.getAirline();
        if(!flight.getHasDeparted() && airline.isInPendingFlights(flight)) {
            flight.setHasDeparted(true);
            flight.addFlightStatus(this.addFlightStatus(LocalDateTime.now(), FlightStatusEnum.DEPARTED));
            Flight flight1 = this.flightRepository.save(flight);
            airline.addDepartedFlight(flight1);
            airline.getPendingFlights().remove(flight1);
            this.airlineRepository.save(airline);
        }
    }
}
