package com.airline.api.exceptions;

public class EmptyPendingFlightsException extends RuntimeException {
    public EmptyPendingFlightsException() {
        super("There are no pending flights in the system");
    }
}
