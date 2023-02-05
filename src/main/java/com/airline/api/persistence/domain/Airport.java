package com.airline.api.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.HashSet;
import java.util.Set;

public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    @Positive
    private Integer planeCount;
    @JsonIgnore
    @OneToMany(mappedBy = "airport", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private Set<Flight> pendingFlights = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "airport", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private Set<Flight> departedFlights = new HashSet<>();

    public boolean addPendingFlight(Flight flight) {
        return this.pendingFlights.add(flight);
    }

    public boolean addDepartedFlight(Flight flight) {
        return this.departedFlights.add(flight);
    }

    public boolean removePendingFlight(Flight flight) {
        return this.pendingFlights.remove(flight);
    }

    public boolean removeDepartedFlight(Flight flight) {
        return this.departedFlights.remove(flight);
    }

    public boolean isInPendingFlights(Flight flight) {
        return this.pendingFlights.contains(flight);
    }
    public boolean isInDepartedFlights(Flight flight) {
        return this.departedFlights.contains(flight);
    }

}
