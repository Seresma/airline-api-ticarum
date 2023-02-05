package com.airline.api.controllers;

import com.airline.api.context.GlobalConfig;
import com.airline.api.dto.FlightDTO;
import com.airline.api.persistence.domain.Airline;
import com.airline.api.persistence.domain.Flight;
import com.airline.api.responses.FlightStatusResponse;
import com.airline.api.services.AirlineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@RequestMapping(GlobalConfig.AIRLINE_NAME)
@Slf4j
@RestController
public class AirlineController {
    private final AirlineService airlineService;

    @GetMapping("/info")
    public Airline getAirlineInfo() {
        return this.airlineService.getInfo();
    }

    @GetMapping("/vuelo")
    public Set<Flight> getPendingFlights() {
        return this.airlineService.getPendingFlights();
    }

    @PostMapping("/vuelo")
    public Flight addFlight(@Valid @RequestBody FlightDTO flightDTO) {
        log.info("Created flight");
        return this.airlineService.addFlight(flightDTO);
    }

    @GetMapping ("/vuelo/{ID_VUELO}")
    public Flight findFlightById(@PathVariable("ID_VUELO") Long id) {
        return this.airlineService.findFlightById(id);
    }

    @PutMapping("/vuelo/{ID_VUELO}")
    public ResponseEntity<Void> updateFlightById(@PathVariable("ID_VUELO") Long id, @RequestBody FlightDTO flightDTO) {
        this.airlineService.updateFlightById(id, flightDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/vuelo/{ID_VUELO}")
    public ResponseEntity<Void> deleteFlightById(@PathVariable("ID_VUELO") Long id) {
        this.airlineService.deleteFlightById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping ("/salida")
    public Set<Flight> getDepartedFlights() {
        return this.airlineService.getDepartedFlights();
    }

    @GetMapping ("/salida/{ID_VUELO}")
    public ResponseEntity<FlightStatusResponse> getFlightStatus(@PathVariable("ID_VUELO") Long id) {
        return ResponseEntity.ok(this.airlineService.getFlightStatus(id));
    }

    @PutMapping ("/salida/{ID_VUELO}/despegue")
    public ResponseEntity<Void> departFlight(@PathVariable("ID_VUELO") Long id) {
        this.airlineService.departFlight(id);
        return ResponseEntity.noContent().build();
    }

}
