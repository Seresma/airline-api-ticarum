package com.airline.api.persistence.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    private String origin;
    private String destination;
    private LocalDateTime etd;
    private LocalDateTime eta;
    private LocalDateTime statusDate;
    private StatusEnum status;
    @ManyToOne
    @JoinColumn(name="plane_id")
    private Plane plane;
}
