package com.airline.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlightStatusResponse {
    private Boolean hasDeparted;
    private LocalDateTime departedDate;
}
