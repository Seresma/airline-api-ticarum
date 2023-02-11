package com.airline.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateFlightDto {
    @Pattern(regexp = "[a-zA-Z ]+", message = "origin can only contain letters")
    private String origin;
    @Pattern(regexp = "[a-zA-Z ]+", message = "destination can only contain letters")
    private String destination;
    @Future(message = "etd must be a future date")
    private LocalDateTime etd;
    @Future(message = "eta must be a future date")
    private LocalDateTime eta;
    @Pattern(regexp = "[A-Z0-9]+-[A-Z0-9]+", message = "planeRegistrationCode must follow this format [A-Z0-9]+-[A-Z0-9]+")
    private String planeRegistrationCode;
}
