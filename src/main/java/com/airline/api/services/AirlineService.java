package com.airline.api.services;

import com.airline.api.context.GlobalConfig;
import com.airline.api.dto.FlightDTO;
import com.airline.api.persistence.domain.Flight;
import com.airline.api.persistence.domain.StatusEnum;
import com.airline.api.persistence.repositories.IFlightRepository;
import com.airline.api.persistence.domain.Airline;
import com.airline.api.persistence.domain.Plane;
import com.airline.api.persistence.repositories.IAirlineRepository;
import com.airline.api.persistence.repositories.IPlaneRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class AirlineService {
    private final IAirlineRepository airlineRepository;
    private final IPlaneRepository planeRepository;
    private final IFlightRepository flightRepository;
    private final ModelMapper modelmapper;

    public Airline getInfo() {
        return this.airlineRepository.findByName(GlobalConfig.AIRLINE_NAME);
    }

    public List<Flight> getPendingFlights() {
        return this.flightRepository.findByStatus(StatusEnum.PENDING);
    }

    public Flight addFlight(FlightDTO flightDTO) {
        Flight flight = modelmapper.map(flightDTO, Flight.class);
        // TODO PUEDE FALLAR
        Plane plane = this.planeRepository.findByRegistrationCode(flightDTO.getPlaneRegistrationCode());
        flight.setPlane(plane);
        flight.setStatus(StatusEnum.PENDING);
        flight.setStatusDate(LocalDateTime.now());
        return this.flightRepository.save(flight);
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
        this.flightRepository.deleteById(id);
    }
}
