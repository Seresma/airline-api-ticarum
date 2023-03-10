package com.airline.api.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
public class Airline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer planeCount;
    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "airline_pending_id")
    private Set<Flight> pendingFlights = new HashSet<>();
    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "airline_departed_id")
    private Set<Flight> departedFlights = new HashSet<>();

    public Airline(String name, Integer planeCount) {
        this.name = name;
        this.planeCount = planeCount;
    }

    public void addPendingFlight(Flight flight) {
        this.pendingFlights.add(flight);
    }

    public void addDepartedFlight(Flight flight) {
        this.pendingFlights.remove(flight);
        this.departedFlights.add(flight);
    }
}
