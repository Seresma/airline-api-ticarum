package com.airline.api.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
class ExceptionBody {
    private LocalDateTime timestamp;
    private HttpStatus status;
    private String message;
    private String path;
}
