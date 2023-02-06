package com.airline.api.controllers;

import com.airline.api.context.GlobalConfig;
import com.airline.api.dto.FlightDTO;
import com.airline.api.persistence.domain.Airline;
import com.airline.api.persistence.domain.Flight;
import com.airline.api.persistence.domain.FlightStatus;
import com.airline.api.responses.FlightStatusResponse;
import com.airline.api.services.AirlineService;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

@AllArgsConstructor
@RequestMapping(GlobalConfig.AIRLINE_NAME)
@Slf4j
@Api(description = "Provides endpoints for managing flights within an airline")
@RestController
public class AirlineController {
    private final AirlineService airlineService;

    @ApiOperation(value = "Returns the airline information", response = Airline.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Airline not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/info")
    public ResponseEntity<Airline> getAirlineInfo() {
        return ResponseEntity.ok(this.airlineService.getInfo());
    }

    @ApiOperation(value = "Finds all pending flights", response = Flight[].class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "No pending flights found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/vuelo")
    public ResponseEntity<Set<Flight>> findAllPendingFlights() {
        return ResponseEntity.ok(this.airlineService.getPendingFlights());
    }

    @ApiOperation(value = "Adds a new flight to the pending flights list", response = Flight.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Flight successfully created"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @PostMapping("/vuelo")
    public ResponseEntity<Flight> addFlight(@ApiParam(value = "Created flight object", required = true) @Valid @RequestBody FlightDTO flight) {
        log.info("Created flight");
        return ResponseEntity.status(HttpStatus.CREATED).body(this.airlineService.addFlight(flight));
    }

    @ApiOperation(value = "Finds a flight by its ID", response = Flight.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Flight not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping ("/vuelo/{ID_VUELO}")
    public ResponseEntity<Flight> findFlightById(@ApiParam(value = "Flight ID", required = true) @PathVariable("ID_VUELO") Long id) {
        return ResponseEntity.ok(this.airlineService.findFlightById(id));
    }

    @ApiOperation(value = "Updates an existing flight by its ID")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Flight not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @PutMapping("/vuelo/{ID_VUELO}")
    public ResponseEntity<Void> updateFlightById(@ApiParam(value = "Flight ID", required = true) @PathVariable("ID_VUELO") Long id, @ApiParam(value = "Updated flight object (no mandatory properties just fill the properties you want to update)", required = true) @RequestBody FlightDTO flight) {
        this.airlineService.updateFlightById(id, flight);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Deletes an existing flight by its ID")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Flight not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @DeleteMapping("/vuelo/{ID_VUELO}")
    public ResponseEntity<Void> deleteFlightById(@ApiParam(value = "Flight ID", required = true) @PathVariable("ID_VUELO") Long id) {
        this.airlineService.deleteFlightById(id);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Finds all departed flights", response = Flight[].class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "No departed flights found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping ("/salida")
    public ResponseEntity<Set<Flight>> findAllDepartedFlights() {
        return ResponseEntity.ok(this.airlineService.getDepartedFlights());
    }

    @ApiOperation(value = "Returns the status of the flight ", response = FlightStatus.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Flight not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping ("/salida/{ID_VUELO}")
    public ResponseEntity<FlightStatusResponse> getFlightStatus(@ApiParam(value = "Flight ID", required = true) @PathVariable("ID_VUELO") Long id) {
        return ResponseEntity.ok(this.airlineService.getFlightStatus(id));
    }

    @ApiOperation(value = "Departs a pending flight")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Flight not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @PutMapping ("/salida/{ID_VUELO}/despegue")
    public ResponseEntity<Void> departFlight(@ApiParam(value = "Flight ID", required = true) @PathVariable("ID_VUELO") Long id) {
        this.airlineService.departFlight(id);
        return ResponseEntity.noContent().build();
    }

}
