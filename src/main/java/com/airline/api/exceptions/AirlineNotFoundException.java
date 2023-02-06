package com.airline.api.exceptions;

public class AirlineNotFoundException extends RuntimeException {
    public AirlineNotFoundException(String name) {
        super("Cannot find airline with name: " + name);
    }
}
