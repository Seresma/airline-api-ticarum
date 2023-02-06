package com.airline.api.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    private String origin;
    private String destination;
    private LocalDateTime etd;
    private LocalDateTime eta;
    private LocalDateTime departDate;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "flight_id")
    private List<FlightStatus> statuses = new ArrayList<>();
    private Boolean hasDeparted;
    @ManyToOne
    @JoinColumn(name="plane_id")
    private Plane plane;
    @JsonIgnore
    public FlightStatus getLastFlightStatus() {
        List<FlightStatus> statuses = this.getStatuses();
        if(statuses.size() > 0)
            return statuses.get(statuses.size() - 1);
        return null;
    }

    public void addFlightStatus(FlightStatus flightStatus){
        this.getStatuses().add(flightStatus);
    }

}
