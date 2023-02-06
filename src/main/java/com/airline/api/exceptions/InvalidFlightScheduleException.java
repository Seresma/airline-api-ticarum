package com.airline.api.exceptions;

public class InvalidFlightScheduleException extends RuntimeException {

    public InvalidFlightScheduleException() {
        super("Estimated date of departure (etd) must be before the estimated date of arrival (eta) and both must be future dates");
    }
}
