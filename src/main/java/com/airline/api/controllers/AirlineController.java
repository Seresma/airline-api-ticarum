package com.airline.api.controllers;

import com.airline.api.context.GlobalConfig;
import com.airline.api.dto.CreateFlightDto;
import com.airline.api.dto.FlightStatusDto;
import com.airline.api.dto.UpdateFlightDto;
import com.airline.api.persistence.model.Airline;
import com.airline.api.persistence.model.Flight;
import com.airline.api.persistence.model.FlightStatus;
import com.airline.api.services.AirlineServiceImpl;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

@AllArgsConstructor
@RequestMapping(GlobalConfig.AIRLINE_NAME)
@Api(description = "Provides endpoints to manage flights within an airline")
@RestController
public class AirlineController {
    private final AirlineServiceImpl airlineService;

    @ApiOperation(value = "Returns the information of the airline", response = Airline.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "You must register to do this operation"),
            @ApiResponse(code = 404, message = "Airline not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/info")
    public Airline getAirlineInfo() {
        return this.airlineService.getAirline();
    }

    @ApiOperation(value = "Finds all pending flights", response = Flight[].class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "You must register to do this operation"),
            @ApiResponse(code = 404, message = "Airline not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/vuelo")
    public Set<Flight> findAllPendingFlights() {
        return this.airlineService.getPendingFlights();
    }

    @ApiOperation(value = "Adds a new flight to the pending flights list", response = Flight.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Flight successfully created"),
            @ApiResponse(code = 400, message = "Invalid data supplied"),
            @ApiResponse(code = 401, message = "You must register to do this operation"),
            @ApiResponse(code = 403, message = "Admin access only"),
            @ApiResponse(code = 404, message = "Airline from plane not found/registration code does not refer to any plane"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @PostMapping("/vuelo")
    public ResponseEntity<Flight> addFlight(@ApiParam(value = "Created flight object", required = true) @Valid @RequestBody CreateFlightDto flight) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.airlineService.addFlight(flight));
    }

    @ApiOperation(value = "Finds a flight by its ID", response = Flight.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Invalid ID"),
            @ApiResponse(code = 401, message = "You must register to do this operation"),
            @ApiResponse(code = 404, message = "Flight not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/vuelo/{ID_VUELO}")
    public Flight findFlightById(@ApiParam(value = "Flight ID", required = true) @PathVariable("ID_VUELO") Long id) {
        return this.airlineService.findFlightById(id);
    }

    @ApiOperation(value = "Updates an existing flight by its ID")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Invalid ID"),
            @ApiResponse(code = 401, message = "You must register to do this operation"),
            @ApiResponse(code = 403, message = "Admin access only"),
            @ApiResponse(code = 404, message = "Flight not found/registration code does not refer to any plane"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @PutMapping("/vuelo/{ID_VUELO}")
    public ResponseEntity<Void> updateFlightById(@ApiParam(value = "Flight ID", required = true) @PathVariable("ID_VUELO") Long id, @ApiParam(value = "Updated flight object (no mandatory properties just fill the properties you want to update)", required = true) @Valid @RequestBody UpdateFlightDto flight) {
        this.airlineService.updateFlightById(id, flight);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "Deletes a flight by its ID")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Invalid ID"),
            @ApiResponse(code = 401, message = "You must register to do this operation"),
            @ApiResponse(code = 403, message = "Admin access only"),
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
            @ApiResponse(code = 401, message = "You must register to do this operation"),
            @ApiResponse(code = 404, message = "Airline not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/salida")
    public Set<Flight> findAllDepartedFlights() {
        return this.airlineService.getDepartedFlights();
    }

    @ApiOperation(value = "Returns the status of the flight", notes = "Return if the flight has departed and if so the departure date", response = FlightStatus.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Invalid ID"),
            @ApiResponse(code = 401, message = "You must register to do this operation"),
            @ApiResponse(code = 404, message = "Flight not found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/salida/{ID_VUELO}")
    public FlightStatusDto getFlightStatus(@ApiParam(value = "Flight ID", required = true) @PathVariable("ID_VUELO") Long id) {
        return this.airlineService.getFlightStatus(id);
    }

    @ApiOperation(value = "Departs a pending flight")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Invalid ID"),
            @ApiResponse(code = 401, message = "You must register to do this operation"),
            @ApiResponse(code = 403, message = "Admin access only/flight is not in the pending list"),
            @ApiResponse(code = 404, message = "Flight not found"),
            @ApiResponse(code = 409, message = "The flight has already departed"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @PutMapping("/salida/{ID_VUELO}/despegue")
    public ResponseEntity<Void> departFlight(@ApiParam(value = "Flight ID", required = true) @PathVariable("ID_VUELO") Long id) {
        this.airlineService.departFlight(id);
        return ResponseEntity.noContent().build();
    }

}
