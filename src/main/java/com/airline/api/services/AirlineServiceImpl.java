package com.airline.api.services;

import com.airline.api.context.GlobalConfig;
import com.airline.api.dto.CreateFlightDto;
import com.airline.api.dto.UpdateFlightDto;
import com.airline.api.exceptions.*;
import com.airline.api.persistence.model.*;
import com.airline.api.persistence.repositories.AirlineRepository;
import com.airline.api.persistence.repositories.FlightRepository;
import com.airline.api.persistence.repositories.FlightStatusRepository;
import com.airline.api.persistence.repositories.PlaneRepository;
import com.airline.api.responses.FlightStatusResponse;
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

    private FlightStatus addFlightStatus(LocalDateTime dateTime, FlightStatusEnum flightStatusEnum) {
        return this.flightStatusRepository.save(new FlightStatus(null, dateTime, flightStatusEnum));
    }

    public Airline getAirline() {
        Airline airline = this.airlineRepository.findByNameIgnoreCase(GlobalConfig.AIRLINE_NAME);
        if (airline == null)
            throw new EntityNotFoundException("Cannot find airline with name: " + GlobalConfig.AIRLINE_NAME);
        return airline;
    }

    public Set<Flight> getPendingFlights() {
        Set<Flight> pendingFlights = this.getAirline().getPendingFlights();
        if(pendingFlights.isEmpty())
            throw new EntityNotFoundException("There are no pending flights in the system");
        else
            return pendingFlights;
    }

    public Flight addFlight(CreateFlightDto flightDTO) {
        Flight flight = modelmapper.map(flightDTO, Flight.class);
        if (!flight.isCorrectSchedule())
            throw new BadRequestException("Estimated date of departure (etd) must be before the estimated date of arrival (eta) and both must be future dates");

        Plane plane = this.planeRepository.findByRegistrationCode(flightDTO.getPlaneRegistrationCode());
        if (plane == null)
            throw new EntityNotFoundException("Cannot find plane with registration code: " + flightDTO.getPlaneRegistrationCode());

        flight.setPlane(plane);
        flight.setHasDeparted(false);
        flight.addFlightStatus(this.addFlightStatus(LocalDateTime.now(), FlightStatusEnum.PENDING));

        Airline airline = this.getAirline();
        airline.addPendingFlight(flight);

        return this.flightRepository.save(flight);
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
            if(etdUpdate.isAfter(LocalDateTime.now()))
                flight.setEtd(etdUpdate);
            else
                throw new BadRequestException("Estimated date of departure (etd) must be before the estimated date of arrival (eta) and both must be future dates");
        }

        LocalDateTime etaUpdate = flightDTO.getEta();
        if(etaUpdate != null) {
            flight.setEta(etaUpdate);
            if(etaUpdate.isBefore(LocalDateTime.now()) || !flight.isCorrectSchedule())
                throw new BadRequestException("Estimated date of departure (etd) must be before the estimated date of arrival (eta) and both must be future dates");
        }

        String registrationCodeUpdate = flightDTO.getPlaneRegistrationCode();
        if(registrationCodeUpdate != null && !registrationCodeUpdate.isBlank()) {
            Plane plane = this.planeRepository.findByRegistrationCode(registrationCodeUpdate);
            if(plane == null)
                throw new EntityNotFoundException("Cannot find plane with registration code: " + flightDTO.getPlaneRegistrationCode());
            flight.setPlane(plane);
        }

        this.flightRepository.save(flight);
    }

    public void deleteFlightById(Long id) {
        Flight flight = this.findFlightById(id);
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
            throw new EntityNotFoundException("There are no departed flights in the system");
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
