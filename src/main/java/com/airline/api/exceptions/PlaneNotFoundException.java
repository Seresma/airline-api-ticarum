package com.airline.api.exceptions;

public class PlaneNotFoundException extends RuntimeException {
    public PlaneNotFoundException(String registrationCode) {
        super("Cannot find plane with registration code: " + registrationCode);
    }
}
