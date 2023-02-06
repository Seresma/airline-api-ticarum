package com.airline.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class CreateFlightDTO {
    @NotBlank(message = "origin must not be empty")
    @Pattern(regexp = "[a-zA-Z ]+", message = "origin can only contain letters")
    private String origin;
    @NotBlank(message = "destination must not be empty")
    @Pattern(regexp = "[a-zA-Z ]+", message = "destination can only contain letters")
    private String destination;
    @NotNull
    @Future(message = "etd must be a future date")
    private LocalDateTime etd;
    @NotNull
    @Future(message = "eta must be a future date")
    private LocalDateTime eta;
    @NotBlank(message = "planeRegistrationCode must not be empty")
    @Pattern(regexp = "[A-Z0-9]+-[A-Z0-9]+", message = "planeRegistrationCode must follow this format [A-Z0-9]+-[A-Z0-9]+")
    private String planeRegistrationCode;
}
