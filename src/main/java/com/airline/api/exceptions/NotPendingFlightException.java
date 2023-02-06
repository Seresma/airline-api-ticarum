package com.airline.api.exceptions;

public class NotPendingFlightException extends RuntimeException {
    public NotPendingFlightException(Long id) {
        super("Flight with ID: " + id + " is not in the pending flights list");
    }
}
