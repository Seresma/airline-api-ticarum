package com.airline.api.persistence.repositories;

import com.airline.api.persistence.domain.FlightStatus;
import com.airline.api.persistence.domain.Plane;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IFlightStatusRepository extends JpaRepository<FlightStatus, Long> {
}
