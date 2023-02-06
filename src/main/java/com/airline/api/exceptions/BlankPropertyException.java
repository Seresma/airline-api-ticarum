package com.airline.api.exceptions;

public class BlankPropertyException extends RuntimeException {

    public BlankPropertyException(String property) {
        super("This property cannot be blank: " + property);
    }
}
