package com.airline.api.exceptions;

public class EmptyDepartedFlightsException extends RuntimeException {
    public EmptyDepartedFlightsException() {
        super("There are no departed flights in the system");
    }
}
