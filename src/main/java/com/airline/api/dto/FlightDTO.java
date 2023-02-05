package com.airline.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class FlightDTO {
    @NotBlank
    private String number;
    @NotBlank
    private String origin;
    @NotBlank
    private String destination;
    @FutureOrPresent
    private LocalDateTime etd;
    @Future
    private LocalDateTime eta;
    @NotBlank
    private String planeRegistrationCode;
}
