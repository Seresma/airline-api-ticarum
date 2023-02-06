package com.airline.api.services;

import com.airline.api.context.GlobalConfig;
import com.airline.api.dto.CreateFlightDTO;
import com.airline.api.dto.UpdateFlightDTO;
import com.airline.api.exceptions.*;
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

    private FlightStatus addFlightStatus(LocalDateTime dateTime, FlightStatusEnum flightStatusEnum) {
        return this.flightStatusRepository.save(new FlightStatus(null, dateTime, flightStatusEnum));
    }

    public Airline getAirline() {
        Airline airline = this.airlineRepository.findByNameIgnoreCase(GlobalConfig.AIRLINE_NAME);
        if (airline == null)
            throw new AirlineNotFoundException(GlobalConfig.AIRLINE_NAME);
        return airline;
    }

    public Set<Flight> getPendingFlights() {
        Set<Flight> pendingFlights = this.getAirline().getPendingFlights();
        if(pendingFlights.isEmpty())
            throw new EmptyPendingFlightsException();
        else
            return pendingFlights;
    }

    public Flight addFlight(CreateFlightDTO flightDTO) {
        Flight flight = modelmapper.map(flightDTO, Flight.class);
        if(!flight.isCorrectSchedule())
            throw new InvalidFlightScheduleException();

        Plane plane = this.planeRepository.findByRegistrationCode(flightDTO.getPlaneRegistrationCode());
        if(plane == null)
            throw new PlaneNotFoundException(flightDTO.getPlaneRegistrationCode());

        flight.setPlane(plane);
        flight.setHasDeparted(false);
        flight.addFlightStatus(this.addFlightStatus(LocalDateTime.now(), FlightStatusEnum.PENDING));

        Airline airline = this.getAirline();
        airline.addPendingFlight(flight);

        return this.flightRepository.save(flight);
    }

    public Flight findFlightById(Long id) {
        return this.flightRepository.findById(id).orElseThrow(() -> new FlightNotFoundException(id));
    }

    @Transactional
    public void updateFlightById(Long id, UpdateFlightDTO flightDTO) {
        Flight flight = this.findFlightById(id);
        String originUpdate = flightDTO.getOrigin();
        if(originUpdate != null) {
            if(originUpdate.isBlank())
                throw new BlankPropertyException("origin");

            flight.setOrigin(originUpdate);
        }

        String destinationUpdate = flightDTO.getDestination();
        if(destinationUpdate != null) {
            if(destinationUpdate.isBlank())
                throw new BlankPropertyException("destination");

            flight.setDestination(destinationUpdate);
        }

        LocalDateTime etdUpdate = flightDTO.getEtd();
        if(etdUpdate != null) {
            if(etdUpdate.isAfter(LocalDateTime.now()))
                flight.setEtd(etdUpdate);
            else
                throw new InvalidFlightScheduleException();
        }

        LocalDateTime etaUpdate = flightDTO.getEta();
        if(etaUpdate != null) {
            flight.setEta(etaUpdate);
            if(etaUpdate.isBefore(LocalDateTime.now()) || !flight.isCorrectSchedule())
                throw new InvalidFlightScheduleException();
        }

        String registrationCodeUpdate = flightDTO.getPlaneRegistrationCode();
        if(registrationCodeUpdate != null && !registrationCodeUpdate.isBlank()) {
            Plane plane = this.planeRepository.findByRegistrationCode(registrationCodeUpdate);
            if(plane == null)
                throw new PlaneNotFoundException(flightDTO.getPlaneRegistrationCode());
            flight.setPlane(plane);
        }

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
        Set<Flight> departedFlights = this.getAirline().getDepartedFlights();
        if(departedFlights.isEmpty())
            throw new EmptyDepartedFlightsException();
        else
            return departedFlights;
    }

    public FlightStatusResponse getFlightStatus(Long id) {
        Flight flight = this.findFlightById(id);
        if(flight.getHasDeparted()){
            return modelmapper.map(flight, FlightStatusResponse.class);
        }
        return new FlightStatusResponse(false, null);
    }

    @Transactional
    public void departFlight(Long id) {
        Flight flight = this.findFlightById(id);
        Airline airline = this.getAirline();
        if(flight.getHasDeparted())
            throw new DepartedFlightException(flight.getId());
        if(!airline.isInPendingFlights(flight)) {
            throw new NotPendingFlightException(flight.getId());
        }
        flight.setHasDeparted(true);
        flight.addFlightStatus(this.addFlightStatus(LocalDateTime.now(), FlightStatusEnum.DEPARTED));
        flight.setDepartDate(LocalDateTime.now());
        airline.addDepartedFlight(flight);
        this.flightRepository.save(flight);
        this.airlineRepository.save(airline);
    }
}
