package com.airline.api.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "registrationCode")
})
public class Plane {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String model;
    private Integer capacity;
    @ManyToOne
    @JoinColumn(name="airline_id")
    private Airline airline;
    private String registrationCode;
}
