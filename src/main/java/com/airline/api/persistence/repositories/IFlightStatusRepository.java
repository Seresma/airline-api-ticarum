package com.airline.api.persistence.repositories;

import com.airline.api.persistence.domain.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IFlightStatusRepository extends JpaRepository<FlightStatus, Long> {
}
