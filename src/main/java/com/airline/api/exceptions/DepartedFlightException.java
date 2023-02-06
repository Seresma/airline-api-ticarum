package com.airline.api.exceptions;

public class DepartedFlightException extends RuntimeException {
    public DepartedFlightException(Long id) {

        super("Flight with ID: " + id + " has already departed");
    }
}
