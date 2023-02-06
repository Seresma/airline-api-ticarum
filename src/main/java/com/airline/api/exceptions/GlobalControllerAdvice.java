package com.airline.api.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(FlightNotFoundException.class)
    public ResponseEntity<?> handleFlightNotFound(FlightNotFoundException ex, WebRequest request) {
        ExceptionBody body =
                new ExceptionBody(LocalDateTime.now(),
                        HttpStatus.NOT_FOUND, ex.getMessage(),
                        ((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<Object>(body, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(EmptyPendingFlightsException.class)
    public ResponseEntity<?> handleEmptyPendingFlights(EmptyPendingFlightsException ex, WebRequest request) {
        ExceptionBody body =
                new ExceptionBody(LocalDateTime.now(),
                        HttpStatus.NOT_FOUND, ex.getMessage(),
                        ((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<Object>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmptyDepartedFlightsException.class)
    public ResponseEntity<?> handleEmptyDepartedFlights(EmptyDepartedFlightsException ex, WebRequest request) {
        ExceptionBody body =
                new ExceptionBody(LocalDateTime.now(),
                        HttpStatus.NOT_FOUND, ex.getMessage(),
                        ((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<Object>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AirlineNotFoundException.class)
    public ResponseEntity<?> handleAirlineNotFound(AirlineNotFoundException ex, WebRequest request) {
        ExceptionBody body =
                new ExceptionBody(LocalDateTime.now(),
                        HttpStatus.NOT_FOUND, ex.getMessage(),
                        ((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<Object>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DepartedFlightException.class)
    public ResponseEntity<?> handleDepartFlightException(DepartedFlightException ex, WebRequest request) {
        ExceptionBody body =
                new ExceptionBody(LocalDateTime.now(),
                        HttpStatus.CONFLICT, ex.getMessage(),
                        ((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<Object>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotPendingFlightException.class)
    public ResponseEntity<?> handleDepartNotPendingFlightException(NotPendingFlightException ex, WebRequest request) {
        ExceptionBody body =
                new ExceptionBody(LocalDateTime.now(),
                        HttpStatus.FORBIDDEN, ex.getMessage(),
                        ((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<Object>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(PlaneNotFoundException.class)
    public ResponseEntity<?> handlePlaneNotFoundException(PlaneNotFoundException ex, WebRequest request) {
        ExceptionBody body =
                new ExceptionBody(LocalDateTime.now(),
                        HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(),
                        ((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<Object>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(InvalidFlightScheduleException.class)
    public ResponseEntity<?> handleInvalidFlightScheduleException(InvalidFlightScheduleException ex, WebRequest request) {
        ExceptionBody body =
                new ExceptionBody(LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST, ex.getMessage(),
                        ((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<Object>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BlankPropertyException.class)
    public ResponseEntity<?> handleBlankPropertyException(BlankPropertyException ex, WebRequest request) {
        ExceptionBody body =
                new ExceptionBody(LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST, ex.getMessage(),
                        ((ServletWebRequest) request).getRequest().getRequestURI());
        return new ResponseEntity<Object>(body, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        ExceptionBody myBody =
                new ExceptionBody(LocalDateTime.now(),
                        status, ex.getMessage(),
                        ((ServletWebRequest) request).getRequest().getRequestURI());
        return ResponseEntity.status(status).headers(headers).body(myBody);
    }
}
