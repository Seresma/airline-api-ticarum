package com.airline.api.exceptions;

public class FlightNotFoundException extends RuntimeException {
    public FlightNotFoundException(Long id) {
        super("Cannot find flight with ID: " + id);
    }
}
